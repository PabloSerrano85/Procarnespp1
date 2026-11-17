package com.example.data.repository

import com.example.data.local.AdjuntoEntity
import com.example.data.local.AdjuntoPersonaEntity
import com.example.data.local.CertificadoEntity
import com.example.data.local.OperadorEntity
import com.example.data.local.PersonaEntity
import com.example.data.local.RequisitoEntity
import com.example.data.local.RoccaDao
import com.example.data.local.SessionPreferences
import com.example.data.local.UsuarioEntity
import com.example.data.remote.GeorefClient
import com.example.data.remote.GeorefSuggestion
import com.example.domain.CuitValidator
import com.example.domain.EstadoCertificado
import com.example.domain.EstadoRegistral
import com.example.domain.RoccaCatalogos
import com.example.domain.RolUsuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class NuevoOperadorInput(
    val tipoPersona: String, // "fisica" | "juridica"
    val personaExistente: Boolean,
    val personaSeleccionadaId: Long? = null,
    val cuit: String,
    val titular: String,
    val dni: String,
    val telefono: String,
    val email: String,
    val nombreEstablecimiento: String,
    val departamento: String,
    val localidad: String,
    val domicilio: String,
    val habilitacionSanitaria: String,
    val categorias: List<String>,
    val actividadesCodigos: List<String> = emptyList(),
    val tiposCarne: List<String>,
    val requisitosCumplidos: Map<String, Boolean>,
)

class RoccaRepository(
    private val dao: RoccaDao,
    private val sessionPrefs: SessionPreferences,
    private val georefClient: GeorefClient = GeorefClient(),
) {
    val operadores: Flow<List<OperadorEntity>> = dao.observeOperadores()
    val personas: Flow<List<PersonaEntity>> = dao.observePersonas()
    val allRequisitos: Flow<List<RequisitoEntity>> = dao.observeAllRequisitos()
    val allAdjuntos: Flow<List<AdjuntoEntity>> = dao.observeAllAdjuntos()
    val allCertificados: Flow<List<CertificadoEntity>> = dao.observeAllCertificados()
    val loggedInEmail: Flow<String?> = sessionPrefs.loggedInEmail

    fun observePersona(id: Long): Flow<PersonaEntity?> = dao.observePersonaById(id)
    fun observeAdjuntosPersona(personaId: Long): Flow<List<AdjuntoPersonaEntity>> =
        dao.observeAdjuntosForPersona(personaId)

    fun observeOperador(id: Long): Flow<OperadorEntity?> = dao.observeOperadorById(id)
    fun observeRequisitos(operadorId: Long): Flow<List<RequisitoEntity>> =
        dao.observeRequisitosForOperador(operadorId)
    fun observeAdjuntos(operadorId: Long): Flow<List<AdjuntoEntity>> =
        dao.observeAdjuntosForOperador(operadorId)
    fun observeCertificados(operadorId: Long): Flow<List<CertificadoEntity>> =
        dao.observeCertificadosForOperador(operadorId)

    suspend fun ensureSeeded() {
        // 1. Seed Personas & Operadores first so Titular Persona links cleanly to La Norita
        val laNoritaPersonaId = dao.getPersonaByIdentificador("30-71234567-8")?.id
            ?: dao.insertPersona(
                PersonaEntity(
                    tipo = "PF",
                    tipoDocumento = "CUIT",
                    identificador = "30-71234567-8",
                    nombre = "OLIVEIRA DANIEL ALBERTO (La Norita)",
                    dni = "30.917.912",
                    telefono = "(0385) 431-8910",
                    email = "titular@lanorita.com",
                )
            )

        val supervisorPersonaId = dao.getPersonaByIdentificador("23-30917912-9")?.id
            ?: dao.insertPersona(
                PersonaEntity(
                    tipo = "PF",
                    tipoDocumento = "CUIT",
                    identificador = "23-30917912-9",
                    nombre = "INSPECCIÓN PROCARNE SDE",
                    dni = "30.917.912",
                    telefono = "(0385) 431-8910",
                    email = "supervisor@procarne.gob.ar",
                )
            )

        val adminPersonaId = dao.getPersonaByIdentificador("30-99900011-2")?.id
            ?: dao.insertPersona(
                PersonaEntity(
                    tipo = "PJ",
                    tipoDocumento = "CUIT",
                    identificador = "30-99900011-2",
                    nombre = "DIRECCIÓN GRAL. AGRICULTURA Y GANADERÍA",
                    telefono = "(0385) 421-3300",
                    email = "admin@procarne.gob.ar",
                )
            )

        // 2. Seed los 3 roles asignados + alias legacy
        val demoAccounts = listOf(
            UsuarioEntity(
                email = "titular@lanorita.com",
                passwordHash = sha256("Procarne2026!"),
                nombre = "Daniel Oliveira (Titular)",
                rol = RolUsuario.PERSONA.key,
                personaId = laNoritaPersonaId,
                creadoEn = nowIso(),
            ),
            UsuarioEntity(
                email = "supervisor@procarne.gob.ar",
                passwordHash = sha256("Procarne2026!"),
                nombre = "Inspector Fiscalización SDE",
                rol = RolUsuario.SUPERVISOR.key,
                personaId = supervisorPersonaId,
                creadoEn = nowIso(),
            ),
            UsuarioEntity(
                email = "admin@procarne.gob.ar",
                passwordHash = sha256("Procarne2026!"),
                nombre = "Autoridad ProCarne (Admin)",
                rol = RolUsuario.ADMIN.key,
                personaId = adminPersonaId,
                creadoEn = nowIso(),
            ),
            UsuarioEntity(
                email = "operador@procarne.gob.ar",
                passwordHash = sha256("Procarne2026!"),
                nombre = "Inspector ProCarne (Demo)",
                rol = RolUsuario.SUPERVISOR.key,
                personaId = supervisorPersonaId,
                creadoEn = nowIso(),
            ),
        )
        demoAccounts.forEach { acc ->
            if (dao.getUsuarioByEmail(acc.email) == null) {
                dao.insertUsuario(acc)
            }
        }

        // 3. Seed Operadores y Certificados de prueba
        if (dao.countOperadores() == 0) {
            val p2 = dao.insertPersona(
                PersonaEntity(
                    tipo = "PJ",
                    tipoDocumento = "CUIT",
                    identificador = "30-82345678-9",
                    nombre = "FRIGORÍFICO EL TRUFO S.R.L.",
                    telefono = "(0385) 442-7721",
                    email = "admin@eltrufosrl.com",
                )
            )
            val p4 = dao.insertPersona(
                PersonaEntity(
                    tipo = "PJ",
                    tipoDocumento = "CUIT",
                    identificador = "30-99988877-0",
                    nombre = "TRANSPORTES CARNE S.A.",
                    email = "logistica@transportescarne.com.ar",
                )
            )

            data class SeedSpec(
                val op: OperadorEntity,
                val daysToExpiry: Int? = 330,
                val emitCert: Boolean = true,
            )

            val seedSpecs = listOf(
                SeedSpec(
                    op = OperadorEntity(
                        personaId = laNoritaPersonaId,
                        tipo = "CARNICERIA",
                        razonSocial = "CARNICERÍA LA NORITA - CASA CENTRAL",
                        nombreEstablecimiento = "LA NORITA CASA CENTRAL",
                        cuit = "30-71234567-8",
                        titular = "OLIVEIRA DANIEL ALBERTO (La Norita)",
                        calle = "JOSE BENJAMIN ABALOS",
                        numero = "4582",
                        departamento = "Capital",
                        localidad = "Santiago del Estero",
                        provincia = "Santiago del Estero",
                        telefono = "(0385) 431-8910",
                        email = "titular@lanorita.com",
                        categoria = "CARNICERIAS",
                        categoriasCsv = "CARNICERIAS",
                        codigoAfipCsv = "922210",
                        actividadesCodigosCsv = "922210",
                        tiposCarneCsv = "Bovina, Porcina",
                        habilitacionSanitaria = "Municipal N° 419/2026 • ProCarne F 102",
                        estadoRegistral = "ACTIVO",
                        altaFecha = "2026-01-15T10:00:00Z",
                        creadoEn = "2026-01-15T10:00:00Z",
                    ),
                    daysToExpiry = 290,
                    emitCert = true,
                ),
                SeedSpec(
                    op = OperadorEntity(
                        personaId = laNoritaPersonaId,
                        tipo = "CARNICERIA",
                        razonSocial = "CARNICERÍA LA NORITA - SUC. LA BANDA",
                        nombreEstablecimiento = "LA NORITA LA BANDA",
                        cuit = "30-71234567-8",
                        titular = "OLIVEIRA DANIEL ALBERTO (La Norita)",
                        calle = "Mitre",
                        numero = "567",
                        departamento = "Banda",
                        localidad = "La Banda",
                        provincia = "Santiago del Estero",
                        telefono = "(0385) 455-9012",
                        email = "sucursal2@lanorita.com",
                        categoria = "CARNICERIAS",
                        categoriasCsv = "CARNICERIAS",
                        codigoAfipCsv = "922210",
                        actividadesCodigosCsv = "922210",
                        tiposCarneCsv = "Bovina, Porcina",
                        habilitacionSanitaria = "Municipal Banda N° 112/2025",
                        estadoRegistral = "ACTIVO",
                        altaFecha = "2025-12-05T09:15:00Z",
                        creadoEn = "2025-12-05T09:15:00Z",
                    ),
                    daysToExpiry = 18, // Próximo a vencer -> visible en alertas Persona y Admin
                    emitCert = true,
                ),
                SeedSpec(
                    op = OperadorEntity(
                        personaId = laNoritaPersonaId,
                        tipo = "CARNICERIA",
                        razonSocial = "CARNICERÍA LA NORITA - SUC. NORTE",
                        nombreEstablecimiento = "LA NORITA BARRIO NORTE",
                        cuit = "30-71234567-8",
                        titular = "OLIVEIRA DANIEL ALBERTO (La Norita)",
                        calle = "Av. Belgrano Norte",
                        numero = "1890",
                        departamento = "Capital",
                        localidad = "Santiago del Estero",
                        provincia = "Santiago del Estero",
                        telefono = "(0385) 431-8910",
                        email = "titular@lanorita.com",
                        categoria = "CARNICERIAS",
                        categoriasCsv = "CARNICERIAS",
                        codigoAfipCsv = "922210",
                        actividadesCodigosCsv = "922210",
                        tiposCarneCsv = "Bovina, Caprina (Cabrito SDE)",
                        habilitacionSanitaria = "En trámite municipal",
                        estadoRegistral = "PENDIENTE",
                        altaFecha = nowIso(),
                        creadoEn = nowIso(),
                    ),
                    daysToExpiry = null,
                    emitCert = false, // Aguardando autorización del Administrador
                ),
                SeedSpec(
                    op = OperadorEntity(
                        personaId = p2,
                        tipo = "FRIGORIFICO",
                        razonSocial = "FRIGORÍFICO EL TRUFO S.R.L.",
                        nombreEstablecimiento = "PLANTA EL TRUFO",
                        cuit = "30-82345678-9",
                        titular = "FRIGORÍFICO EL TRUFO S.R.L.",
                        calle = "AV. SAN MARTÍN",
                        numero = "1234",
                        departamento = "Capital",
                        localidad = "Santiago del Estero",
                        provincia = "Santiago del Estero",
                        telefono = "(0385) 442-7721",
                        email = "admin@eltrufosrl.com",
                        categoria = "SALA_DE_DESPOSTE",
                        categoriasCsv = "SALA_DE_DESPOSTE,FRIGORIFICO",
                        codigoAfipCsv = "101120,101111",
                        actividadesCodigosCsv = "101120,101111",
                        tiposCarneCsv = "Bovina, Porcina, Caprina (Cabrito SDE)",
                        habilitacionSanitaria = "SENASA / ProCarne N° 208/2026",
                        estadoRegistral = "ACTIVO",
                        altaFecha = "2026-02-20T14:30:00Z",
                        creadoEn = "2026-02-20T14:30:00Z",
                    ),
                    daysToExpiry = 240,
                    emitCert = true,
                ),
                SeedSpec(
                    op = OperadorEntity(
                        personaId = p4,
                        tipo = "TRANSPORTE",
                        razonSocial = "TRANSPORTES CARNE S.A.",
                        nombreEstablecimiento = "FLOTA TÉRMICA NORTE",
                        cuit = "30-99988877-0",
                        titular = "TRANSPORTES CARNE S.A.",
                        calle = "Ruta Nacional 9",
                        numero = "KM 45",
                        departamento = "Río Hondo",
                        localidad = "Termas de Río Hondo",
                        provincia = "Santiago del Estero",
                        categoria = "TRANSPONEDOR",
                        categoriasCsv = "TRANSPONEDOR",
                        codigoAfipCsv = "492110",
                        actividadesCodigosCsv = "492110",
                        tiposCarneCsv = "Bovina, Caprina (Cabrito SDE)",
                        habilitacionSanitaria = "RUCA Transporte N° 881/2026",
                        estadoRegistral = "PENDIENTE",
                        altaFecha = "2026-04-01T08:00:00Z",
                        creadoEn = "2026-04-01T08:00:00Z",
                    ),
                    daysToExpiry = null,
                    emitCert = false,
                ),
            )

            seedSpecs.forEach { spec ->
                val opId = dao.insertOperador(spec.op)
                crearRequisitosIniciales(
                    operadorId = opId,
                    categorias = spec.op.categoriasLista,
                    cumplidoInicial = spec.op.estadoRegistral == "ACTIVO",
                )
                if (spec.emitCert) {
                    dao.insertCertificado(
                        CertificadoEntity(
                            operadorId = opId,
                            numeroCertificado = "F102-${100000L + opId}",
                            tipoCertificado = "HABILITACION",
                            fechaEmision = spec.op.altaFecha ?: nowIso(),
                            fechaVencimiento = daysFromNowIso(spec.daysToExpiry ?: 365),
                            actividadesEconomicas = spec.op.categoriasLista.joinToString(", ") {
                                RoccaCatalogos.nombreCategoria(it)
                            },
                            estado = EstadoCertificado.ACTIVO.name,
                            observaciones = spec.op.habilitacionSanitaria,
                            creadoEn = nowIso(),
                        )
                    )
                }
            }
        }
    }

    // --- Auth (CU-10) ---
    suspend fun getUsuarioActual(): UsuarioEntity? {
        val email = sessionPrefs.loggedInEmail.first() ?: return null
        return dao.getUsuarioByEmail(email)
    }

    suspend fun login(email: String, password: String): Result<UsuarioEntity> {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Complete todos los campos obligatorios."))
        }
        val user = dao.getUsuarioByEmail(cleanEmail)
            ?: return Result.failure(IllegalArgumentException("Email o contraseña incorrectos."))
        if (user.passwordHash != sha256(password)) {
            return Result.failure(IllegalArgumentException("Email o contraseña incorrectos."))
        }
        sessionPrefs.setLoggedInEmail(user.email)
        return Result.success(user)
    }

    suspend fun loginAsDemoRole(rol: RolUsuario): Result<UsuarioEntity> {
        val demoEmail = when (rol) {
            RolUsuario.PERSONA -> "titular@lanorita.com"
            RolUsuario.SUPERVISOR -> "supervisor@procarne.gob.ar"
            RolUsuario.ADMIN -> "admin@procarne.gob.ar"
        }
        val user = dao.getUsuarioByEmail(demoEmail)
            ?: return Result.failure(IllegalStateException("Usuario demo no encontrado."))
        sessionPrefs.setLoggedInEmail(user.email)
        return Result.success(user)
    }

    suspend fun signup(
        nombre: String,
        tipoDocumento: String,
        documento: String,
        email: String,
        password: String,
        rol: RolUsuario = RolUsuario.PERSONA,
    ): Result<UsuarioEntity> {
        val cleanNombre = nombre.trim()
        if (cleanNombre.isBlank()) {
            return Result.failure(IllegalArgumentException("Ingrese su nombre de usuario o razón social."))
        }
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Ingrese un email válido."))
        }
        if (password.length < 8) {
            return Result.failure(IllegalArgumentException("La contraseña debe tener al menos 8 caracteres."))
        }
        if (dao.getUsuarioByEmail(cleanEmail) != null) {
            return Result.failure(IllegalArgumentException("Ya existe una cuenta registrada con ese email."))
        }

        val docValidado = if (tipoDocumento == "CUIT") {
            CuitValidator.validarCuit(documento)
        } else {
            CuitValidator.validarDni(documento)
        }
        val docNorm = docValidado.getOrElse { return Result.failure(it) }

        val existingPersona = dao.getPersonaByIdentificador(docNorm)
        val personaId = existingPersona?.id ?: dao.insertPersona(
            PersonaEntity(
                tipo = if (tipoDocumento == "CUIT") "PF" else "PF",
                tipoDocumento = tipoDocumento,
                identificador = docNorm,
                nombre = cleanNombre,
                email = cleanEmail,
            )
        )

        val user = UsuarioEntity(
            email = cleanEmail,
            passwordHash = sha256(password),
            nombre = cleanNombre,
            rol = rol.key,
            personaId = personaId,
            creadoEn = nowIso(),
        )
        val userId = dao.insertUsuario(user)
        val saved = user.copy(id = userId)
        sessionPrefs.setLoggedInEmail(saved.email)
        return Result.success(saved)
    }

    suspend fun logout() {
        sessionPrefs.setLoggedInEmail(null)
    }

    // --- Adjuntos de Persona (CU-4) ---
    suspend fun agregarAdjuntoPersona(
        personaId: Long,
        tipoDocumento: String,
        filename: String,
        contentType: String,
        uriString: String,
        sizeBytes: Long,
    ) {
        dao.insertAdjuntoPersona(
            AdjuntoPersonaEntity(
                personaId = personaId,
                tipoDocumento = tipoDocumento,
                filename = filename,
                contentType = contentType,
                uriString = uriString,
                sizeBytes = sizeBytes,
                creadoEn = nowIso(),
            )
        )
    }

    suspend fun eliminarAdjuntoPersona(adjuntoId: Long) {
        dao.deleteAdjuntoPersona(adjuntoId)
    }

    // --- Operador CRUD & Business Logic (CU-1, CU-2, CU-3, CU-6, CU-7) ---
    suspend fun registrarOperador(
        input: NuevoOperadorInput,
        callerRol: RolUsuario,
    ): Result<OperadorEntity> {
        val categorias = input.categorias.ifEmpty { listOf("CARNICERIAS") }
        val primaryCat = categorias.first()
        val razonSocial = input.titular.trim().ifBlank { input.nombreEstablecimiento.trim() }
        val establecimiento = input.nombreEstablecimiento.trim().ifBlank { razonSocial }

        if (razonSocial.isBlank()) {
            return Result.failure(IllegalArgumentException("Debe ingresar el titular o razón social."))
        }
        if (establecimiento.isBlank()) {
            return Result.failure(IllegalArgumentException("Debe ingresar el nombre del establecimiento."))
        }

        val persona: PersonaEntity = if (input.personaExistente) {
            val byId = input.personaSeleccionadaId?.let { dao.getPersonaById(it) }
            val byCuit = if (input.cuit.isNotBlank()) {
                dao.getPersonaByIdentificador(CuitValidator.formatearCuit(input.cuit))
                    ?: dao.getPersonaByIdentificador(input.cuit.trim())
            } else null
            val sessionUser = getUsuarioActual()
            val bySession = sessionUser?.personaId?.let { dao.getPersonaById(it) }
            byId ?: byCuit ?: bySession
                ?: return Result.failure(
                    IllegalArgumentException("No se encontró la persona existente. Seleccione un titular o ingrese un CUIT registrado.")
                )
        } else {
            val cuitCheck = CuitValidator.validarCuit(input.cuit)
            val cuitFmt = cuitCheck.getOrElse { return Result.failure(it) }
            val existing = dao.getPersonaByIdentificador(cuitFmt)
            if (existing != null) {
                existing
            } else {
                val p = PersonaEntity(
                    tipo = if (input.tipoPersona == "juridica") "PJ" else "PF",
                    tipoDocumento = "CUIT",
                    identificador = cuitFmt,
                    nombre = razonSocial,
                    dni = input.dni.trim().ifBlank { null },
                    telefono = input.telefono.trim().ifBlank { null },
                    email = input.email.trim().ifBlank { null },
                )
                val pid = dao.insertPersona(p)
                p.copy(id = pid)
            }
        }

        if (input.email.isNotBlank() && persona.email != input.email.trim()) {
            dao.updatePersona(persona.copy(email = input.email.trim()))
        }

        val domParsed = RoccaCatalogos.parsearDomicilio(
            texto = input.domicilio,
            departamento = input.departamento.trim(),
            localidadFallback = input.localidad.trim(),
        )
        val codigosAfip = categorias.map { RoccaCatalogos.codigoAfipParaCategoria(it) }.distinct()
        val actividades = input.actividadesCodigos.ifEmpty { codigosAfip }
        val tiposCarne = input.tiposCarne.ifEmpty { listOf("Bovina") }

        val obligatorios = RoccaCatalogos.requisitosParaCategorias(categorias).filter { it.obligatorio }
        val todosObligatoriosCumplidos = obligatorios.all { input.requisitosCumplidos[it.codigo] == true }

        // Regla de roles: Persona crea sucursal en PENDIENTE; Admin puede autorizar en el acto si requisitos están completos.
        val estadoInicial = if (callerRol == RolUsuario.ADMIN && todosObligatoriosCumplidos) {
            EstadoRegistral.ACTIVO.name
        } else {
            EstadoRegistral.PENDIENTE.name
        }

        val now = nowIso()
        val op = OperadorEntity(
            personaId = persona.id,
            tipo = primaryCat,
            razonSocial = if (input.nombreEstablecimiento.isNotBlank() &&
                !input.nombreEstablecimiento.equals(persona.nombre, ignoreCase = true)
            ) {
                "${persona.nombre} - ${input.nombreEstablecimiento.trim()}"
            } else {
                razonSocial
            },
            nombreEstablecimiento = establecimiento,
            cuit = persona.identificador,
            titular = persona.nombre,
            calle = domParsed.calle.ifBlank { input.domicilio.trim() },
            numero = domParsed.numero,
            departamento = input.departamento.trim().ifBlank { domParsed.departamento },
            localidad = input.localidad.trim().ifBlank { domParsed.localidad },
            provincia = domParsed.provincia.ifBlank { "Santiago del Estero" },
            telefono = input.telefono.trim().ifBlank { persona.telefono },
            email = input.email.trim().ifBlank { persona.email },
            categoria = primaryCat,
            categoriasCsv = categorias.joinToString(","),
            codigoAfipCsv = codigosAfip.joinToString(","),
            actividadesCodigosCsv = actividades.joinToString(","),
            tiposCarneCsv = tiposCarne.joinToString(", "),
            habilitacionSanitaria = input.habilitacionSanitaria.trim().ifBlank { null },
            estadoRegistral = estadoInicial,
            altaFecha = now,
            creadoEn = now,
        )

        val opId = dao.insertOperador(op)
        val savedOp = op.copy(id = opId)

        val reqEntities = RoccaCatalogos.requisitosParaCategorias(categorias).map { catReq ->
            RequisitoEntity(
                operadorId = opId,
                codigo = catReq.codigo,
                nombre = catReq.nombre,
                esGeneral = catReq.esGeneral,
                obligatorio = catReq.obligatorio,
                cumplido = input.requisitosCumplidos[catReq.codigo] == true,
            )
        }
        dao.insertRequisitos(reqEntities)

        if (estadoInicial == EstadoRegistral.ACTIVO.name) {
            emitirCertificadoInicial(opId, savedOp)
        }

        return Result.success(savedOp)
    }

    suspend fun cambiarEstadoOperador(operadorId: Long, nuevoEstado: EstadoRegistral): Result<Unit> {
        val op = dao.getOperadorById(operadorId)
            ?: return Result.failure(IllegalArgumentException("Operador no encontrado"))
        val actual = EstadoRegistral.fromString(op.estadoRegistral)
        if (actual == EstadoRegistral.BAJADO && nuevoEstado == EstadoRegistral.ACTIVO) {
            return Result.failure(IllegalArgumentException("No se puede activar un operador dado de baja."))
        }
        val bajaFecha = if (nuevoEstado == EstadoRegistral.BAJADO) nowIso() else null
        dao.updateEstadoOperador(operadorId, nuevoEstado.name, bajaFecha)
        return Result.success(Unit)
    }

    suspend fun actualizarRequisito(requisitoId: Long, cumplido: Boolean, observaciones: String?) {
        dao.updateRequisitoCumplimiento(requisitoId, cumplido, observaciones?.trim()?.ifBlank { null })
    }

    suspend fun agregarAdjunto(
        operadorId: Long,
        requisitoCodigo: String,
        filename: String,
        contentType: String,
        uriString: String,
        sizeBytes: Long,
    ) {
        dao.insertAdjunto(
            AdjuntoEntity(
                operadorId = operadorId,
                requisitoCodigo = requisitoCodigo,
                filename = filename,
                contentType = contentType,
                uriString = uriString,
                sizeBytes = sizeBytes,
                creadoEn = nowIso(),
            )
        )
    }

    suspend fun eliminarAdjunto(adjuntoId: Long) {
        dao.deleteAdjunto(adjuntoId)
    }

    /**
     * Regla CU-7 (Admin): Autoriza si está PENDIENTE/SUSPENDIDO y emite el Cartón Oficial F 102,
     * relevando cualquier certificado ACTIVO previo a RELEVADO.
     */
    suspend fun autorizarYEmitirCertificado(
        operadorId: Long,
        observaciones: String? = null,
    ): Result<CertificadoEntity> {
        val op = dao.getOperadorById(operadorId)
            ?: return Result.failure(IllegalArgumentException("Operador no encontrado"))
        val actual = EstadoRegistral.fromString(op.estadoRegistral)
        if (actual == EstadoRegistral.BAJADO) {
            return Result.failure(IllegalArgumentException("No se puede emitir certificado a un operador dado de baja."))
        }
        if (actual != EstadoRegistral.ACTIVO) {
            dao.updateEstadoOperador(operadorId, EstadoRegistral.ACTIVO.name, null)
        }
        return emitirNuevoCertificado(operadorId, observaciones ?: "Autorizado y emitido por Administrador")
    }

    suspend fun emitirNuevoCertificado(operadorId: Long, observaciones: String? = null): Result<CertificadoEntity> {
        val op = dao.getOperadorById(operadorId)
            ?: return Result.failure(IllegalArgumentException("Operador no encontrado"))
        if (EstadoRegistral.fromString(op.estadoRegistral) != EstadoRegistral.ACTIVO) {
            return Result.failure(
                IllegalArgumentException("El operador debe estar HABILITADO (ACTIVO) para emitir un nuevo Cartón F 102.")
            )
        }
        val existentes = dao.getCertificadosForOperador(operadorId)
        existentes.filter { it.estado == EstadoCertificado.ACTIVO.name }.forEach { prev ->
            dao.updateEstadoCertificado(
                prev.id,
                EstadoCertificado.RELEVADO.name,
                prev.observaciones,
                prev.motivoSuspension,
            )
        }
        val nuevo = CertificadoEntity(
            operadorId = operadorId,
            numeroCertificado = "F102-${op.numeroCertificadoF102}",
            tipoCertificado = "HABILITACION",
            fechaEmision = nowIso(),
            fechaVencimiento = daysFromNowIso(365),
            actividadesEconomicas = op.categoriasLista.joinToString(", ") { RoccaCatalogos.nombreCategoria(it) },
            estado = EstadoCertificado.ACTIVO.name,
            observaciones = observaciones,
            creadoEn = nowIso(),
        )
        val id = dao.insertCertificado(nuevo)
        return Result.success(nuevo.copy(id = id))
    }

    suspend fun suspenderCertificado(certificadoId: Long, motivo: String) {
        val all = dao.observeAllCertificados().first()
        val cert = all.firstOrNull { it.id == certificadoId } ?: return
        dao.updateEstadoCertificado(
            certificadoId = cert.id,
            estado = EstadoCertificado.SUSPENDIDO.name,
            observaciones = cert.observaciones,
            motivoSuspension = motivo.trim().ifBlank { "Suspendido por Administrador" },
        )
    }

    suspend fun reactivarCertificado(certificadoId: Long) {
        val all = dao.observeAllCertificados().first()
        val cert = all.firstOrNull { it.id == certificadoId } ?: return
        dao.updateEstadoCertificado(
            certificadoId = cert.id,
            estado = EstadoCertificado.ACTIVO.name,
            observaciones = cert.observaciones,
            motivoSuspension = null,
        )
    }

    // --- Georef AR ---
    suspend fun buscarDepartamentos(query: String): List<GeorefSuggestion> =
        georefClient.buscarDepartamentos(query)

    suspend fun buscarLocalidades(query: String, departamento: String? = null): List<GeorefSuggestion> =
        georefClient.buscarLocalidades(query, departamento)

    private suspend fun crearRequisitosIniciales(
        operadorId: Long,
        categorias: List<String>,
        cumplidoInicial: Boolean,
    ) {
        val list = RoccaCatalogos.requisitosParaCategorias(categorias).map { req ->
            RequisitoEntity(
                operadorId = operadorId,
                codigo = req.codigo,
                nombre = req.nombre,
                esGeneral = req.esGeneral,
                obligatorio = req.obligatorio,
                cumplido = cumplidoInicial && req.obligatorio,
            )
        }
        dao.insertRequisitos(list)
    }

    private suspend fun emitirCertificadoInicial(operadorId: Long, op: OperadorEntity) {
        dao.insertCertificado(
            CertificadoEntity(
                operadorId = operadorId,
                numeroCertificado = "F102-${100000L + operadorId}",
                tipoCertificado = "HABILITACION",
                fechaEmision = op.altaFecha ?: nowIso(),
                fechaVencimiento = daysFromNowIso(365),
                actividadesEconomicas = op.categoriasLista.joinToString(", ") { RoccaCatalogos.nombreCategoria(it) },
                estado = EstadoCertificado.ACTIVO.name,
                observaciones = op.habilitacionSanitaria,
                creadoEn = nowIso(),
            )
        )
    }

    private fun sha256(text: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(text.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun nowIso(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    private fun daysFromNowIso(days: Int): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.add(Calendar.DAY_OF_YEAR, days)
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(cal.time)
    }
}
