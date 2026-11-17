package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AdjuntoEntity
import com.example.data.local.AdjuntoPersonaEntity
import com.example.data.local.CertificadoEntity
import com.example.data.local.OperadorEntity
import com.example.data.local.PersonaEntity
import com.example.data.local.RequisitoEntity
import com.example.data.local.RoccaDatabase
import com.example.data.local.SessionPreferences
import com.example.data.local.UsuarioEntity
import com.example.data.remote.GeorefSuggestion
import com.example.data.repository.NuevoOperadorInput
import com.example.data.repository.RoccaRepository
import com.example.domain.CuitValidator
import com.example.domain.EstadoRegistral
import com.example.domain.RolUsuario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GrupoPersonaUi(
    val cuit: String,
    val nombreTitular: String,
    val personaId: Long,
    val sucursales: List<OperadorEntity>,
)

data class AuthUiState(
    val checkingSession: Boolean = true,
    val usuario: UsuarioEntity? = null,
    val loading: Boolean = false,
    val error: String? = null,
)

data class FormF102UiState(
    val tipoPersona: String = "fisica", // "fisica" | "juridica"
    val personaExistente: Boolean = false,
    val personaSeleccionadaId: Long? = null,
    val cuit: String = "",
    val cuitError: String? = null,
    val titular: String = "",
    val dni: String = "",
    val telefono: String = "",
    val email: String = "",
    val nombreEstablecimiento: String = "",
    val departamento: String = "",
    val localidad: String = "",
    val domicilio: String = "",
    val habilitacionSanitaria: String = "",
    val categorias: List<String> = listOf("CARNICERIAS"),
    val actividadesCodigos: List<String> = listOf("922210"),
    val tiposCarne: List<String> = listOf("Bovina", "Porcina"),
    val requisitosCumplidos: Map<String, Boolean> = emptyMap(),
    val submitting: Boolean = false,
    val submitError: String? = null,
    val lastCreatedOperador: OperadorEntity? = null,
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class RoccaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = RoccaDatabase.getInstance(application)
    private val sessionPrefs = SessionPreferences(application)
    private val repository = RoccaRepository(db.roccaDao(), sessionPrefs)

    private val _authState = MutableStateFlow(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    val currentRole: StateFlow<RolUsuario> = _authState
        .map { RolUsuario.fromString(it.usuario?.rol) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RolUsuario.PERSONA)

    val personas: StateFlow<List<PersonaEntity>> = repository.personas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentPersona: StateFlow<PersonaEntity?> = combine(_authState, personas) { auth, list ->
        val pid = auth.usuario?.personaId ?: return@combine null
        list.firstOrNull { it.id == pid }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val adjuntosPersona: StateFlow<List<AdjuntoPersonaEntity>> = _authState
        .flatMapLatest { auth ->
            val pid = auth.usuario?.personaId
            if (pid != null) repository.observeAdjuntosPersona(pid) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    private val _filtroEstado = MutableStateFlow<EstadoRegistral?>(null)
    val filtroEstado: StateFlow<EstadoRegistral?> = _filtroEstado.asStateFlow()

    private val _filtroCategoria = MutableStateFlow<String?>(null)
    val filtroCategoria: StateFlow<String?> = _filtroCategoria.asStateFlow()

    private val _formState = MutableStateFlow(FormF102UiState())
    val formState: StateFlow<FormF102UiState> = _formState.asStateFlow()

    private val _depSuggestions = MutableStateFlow<List<GeorefSuggestion>>(emptyList())
    val depSuggestions: StateFlow<List<GeorefSuggestion>> = _depSuggestions.asStateFlow()

    private val _locSuggestions = MutableStateFlow<List<GeorefSuggestion>>(emptyList())
    val locSuggestions: StateFlow<List<GeorefSuggestion>> = _locSuggestions.asStateFlow()

    private val _depQuery = MutableStateFlow("")
    private val _locQuery = MutableStateFlow("")

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    val todosOperadores: StateFlow<List<OperadorEntity>> = repository.operadores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRequisitos: StateFlow<List<RequisitoEntity>> = repository.allRequisitos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAdjuntos: StateFlow<List<AdjuntoEntity>> = repository.allAdjuntos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCertificados: StateFlow<List<CertificadoEntity>> = repository.allCertificados
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * CU-5 / CU-9: Persona ve solo sus sucursales por CUIT/personaId;
     * Supervisor y Administrador ven el padrón completo.
     */
    val operadoresVisibles: StateFlow<List<OperadorEntity>> = combine(
        todosOperadores,
        _authState,
    ) { ops, auth ->
        val user = auth.usuario ?: return@combine emptyList()
        if (RolUsuario.fromString(user.rol) == RolUsuario.PERSONA) {
            ops.filter { op -> user.personaId != null && op.personaId == user.personaId }
        } else {
            ops
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val operadoresFiltrados: StateFlow<List<OperadorEntity>> = combine(
        operadoresVisibles,
        _busqueda,
        _filtroEstado,
        _filtroCategoria,
    ) { ops, query, estadoFiltro, categoriaFiltro ->
        val q = query.trim().lowercase()
        ops.filter { op ->
            val matchEstado = estadoFiltro == null ||
                EstadoRegistral.fromString(op.estadoRegistral) == estadoFiltro
            val matchCat = categoriaFiltro == null ||
                op.categoria.equals(categoriaFiltro, ignoreCase = true) ||
                op.categoriasLista.any { it.equals(categoriaFiltro, ignoreCase = true) }
            val matchQuery = q.isEmpty() ||
                op.numeroCertificadoF102.contains(q) ||
                op.razonSocial.lowercase().contains(q) ||
                op.nombreEstablecimiento.lowercase().contains(q) ||
                op.titular.lowercase().contains(q) ||
                op.cuit.lowercase().contains(q) ||
                op.domicilioFormateado.lowercase().contains(q)
            matchEstado && matchCat && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gruposJerarquia: StateFlow<List<GrupoPersonaUi>> = operadoresVisibles
        .combine(personas) { ops, _ ->
            ops.groupBy { it.cuit.uppercase() }
                .entries
                .sortedBy { it.key }
                .map { (cuit, sucursales) ->
                    val sorted = sucursales.sortedBy { it.razonSocial }
                    GrupoPersonaUi(
                        cuit = cuit,
                        nombreTitular = sorted.first().titular.ifBlank { sorted.first().razonSocial },
                        personaId = sorted.first().personaId,
                        sucursales = sorted,
                    )
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            val user = repository.getUsuarioActual()
            _authState.update { it.copy(checkingSession = false, usuario = user) }
            if (user != null && RolUsuario.fromString(user.rol) == RolUsuario.PERSONA) {
                user.personaId?.let { pid ->
                    personas.value.firstOrNull { it.id == pid }?.let { prefillSucursalParaPersona(it) }
                }
            }
        }

        viewModelScope.launch {
            _depQuery.debounce(350).distinctUntilChanged().collect { q ->
                _depSuggestions.value = repository.buscarDepartamentos(q)
            }
        }

        viewModelScope.launch {
            _locQuery.debounce(350).distinctUntilChanged().collect { q ->
                _locSuggestions.value = repository.buscarLocalidades(q, _formState.value.departamento)
            }
        }
    }

    // --- Auth Actions (CU-10) ---
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.update { it.copy(loading = true, error = null) }
            val res = repository.login(email, password)
            res.fold(
                onSuccess = { user ->
                    _authState.update { it.copy(loading = false, usuario = user, error = null) }
                    onUserLoggedIn(user)
                },
                onFailure = { err ->
                    _authState.update { it.copy(loading = false, error = err.message) }
                },
            )
        }
    }

    fun loginAsDemoRole(rol: RolUsuario) {
        viewModelScope.launch {
            _authState.update { it.copy(loading = true, error = null) }
            val res = repository.loginAsDemoRole(rol)
            res.fold(
                onSuccess = { user ->
                    _authState.update { it.copy(loading = false, usuario = user, error = null) }
                    onUserLoggedIn(user)
                    showSnackbar("Sesión iniciada como ${rol.label}.")
                },
                onFailure = { err ->
                    _authState.update { it.copy(loading = false, error = err.message) }
                },
            )
        }
    }

    fun signup(
        nombre: String,
        tipoDoc: String,
        documento: String,
        email: String,
        password: String,
        passwordConfirm: String,
        rol: RolUsuario,
    ) {
        if (password != passwordConfirm) {
            _authState.update { it.copy(error = "Las contraseñas no coinciden.") }
            return
        }
        viewModelScope.launch {
            _authState.update { it.copy(loading = true, error = null) }
            val res = repository.signup(nombre, tipoDoc, documento, email, password, rol)
            res.fold(
                onSuccess = { user ->
                    _authState.update { it.copy(loading = false, usuario = user, error = null) }
                    onUserLoggedIn(user)
                    showSnackbar("Cuenta creada y entidad Persona generada (${rol.label}).")
                },
                onFailure = { err ->
                    _authState.update { it.copy(loading = false, error = err.message) }
                },
            )
        }
    }

    private fun onUserLoggedIn(user: UsuarioEntity) {
        if (RolUsuario.fromString(user.rol) == RolUsuario.PERSONA) {
            val persona = personas.value.firstOrNull { it.id == user.personaId }
            if (persona != null) {
                prefillSucursalParaPersona(persona)
            }
        } else {
            resetFormulario()
        }
        _busqueda.value = ""
        _filtroEstado.value = null
        _filtroCategoria.value = null
    }

    fun clearAuthError() {
        _authState.update { it.copy(error = null) }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _authState.update { AuthUiState(checkingSession = false, usuario = null) }
            resetFormulario()
        }
    }

    // --- Padrón Actions (CU-5) ---
    fun setBusqueda(q: String) {
        _busqueda.value = q
    }

    fun setFiltroEstado(estado: EstadoRegistral?) {
        _filtroEstado.value = estado
    }

    fun setFiltroCategoria(categoria: String?) {
        _filtroCategoria.value = categoria
    }

    // --- Adjuntos de Persona (CU-4) ---
    fun agregarAdjuntoPersona(
        tipoDocumento: String,
        filename: String,
        contentType: String,
        uriString: String,
        sizeBytes: Long,
    ) {
        val pid = _authState.value.usuario?.personaId ?: return
        viewModelScope.launch {
            repository.agregarAdjuntoPersona(pid, tipoDocumento, filename, contentType, uriString, sizeBytes)
            showSnackbar("Documento personal adjuntado: $filename")
        }
    }

    fun eliminarAdjuntoPersona(adjuntoId: Long) {
        viewModelScope.launch {
            repository.eliminarAdjuntoPersona(adjuntoId)
            showSnackbar("Documento personal eliminado.")
        }
    }

    // --- Detail / Lifecycle / Certificados / Requisitos ---
    fun observeOperador(id: Long) = repository.observeOperador(id)
    fun observeRequisitos(operadorId: Long): StateFlow<List<RequisitoEntity>> =
        repository.observeRequisitos(operadorId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun observeAdjuntos(operadorId: Long): StateFlow<List<AdjuntoEntity>> =
        repository.observeAdjuntos(operadorId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun observeCertificados(operadorId: Long): StateFlow<List<CertificadoEntity>> =
        repository.observeCertificados(operadorId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun cambiarEstadoOperador(operadorId: Long, nuevoEstado: EstadoRegistral) {
        if (currentRole.value == RolUsuario.PERSONA) {
            showSnackbar("Solo un Supervisor o Administrador puede cambiar el estado registral.")
            return
        }
        viewModelScope.launch {
            val res = repository.cambiarEstadoOperador(operadorId, nuevoEstado)
            res.fold(
                onSuccess = { showSnackbar("Estado actualizado a ${nuevoEstado.labelUi}.") },
                onFailure = { showSnackbar(it.message ?: "Error al cambiar estado.") },
            )
        }
    }

    fun actualizarRequisito(requisito: RequisitoEntity, cumplido: Boolean, observaciones: String?) {
        viewModelScope.launch {
            repository.actualizarRequisito(requisito.id, cumplido, observaciones)
            showSnackbar("Requisito actualizado.")
        }
    }

    fun toggleRequisitoItem(requisito: RequisitoEntity) {
        viewModelScope.launch {
            repository.actualizarRequisito(requisito.id, !requisito.cumplido, requisito.observaciones)
        }
    }

    fun agregarAdjunto(
        operadorId: Long,
        requisitoCodigo: String,
        filename: String,
        contentType: String,
        uriString: String,
        sizeBytes: Long,
    ) {
        viewModelScope.launch {
            repository.agregarAdjunto(operadorId, requisitoCodigo, filename, contentType, uriString, sizeBytes)
            showSnackbar("Documento adjuntado: $filename")
        }
    }

    fun eliminarAdjunto(adjuntoId: Long) {
        viewModelScope.launch {
            repository.eliminarAdjunto(adjuntoId)
            showSnackbar("Adjunto eliminado.")
        }
    }

    fun autorizarYEmitirCertificado(operadorId: Long) {
        if (currentRole.value != RolUsuario.ADMIN) {
            showSnackbar("Solo el rol Administrador puede autorizar y emitir certificados.")
            return
        }
        viewModelScope.launch {
            val res = repository.autorizarYEmitirCertificado(operadorId)
            res.fold(
                onSuccess = { cert ->
                    showSnackbar("Operador habilitado y Certificado ${cert.numeroCertificado} emitido.")
                },
                onFailure = { showSnackbar(it.message ?: "No se pudo autorizar el certificado.") },
            )
        }
    }

    fun emitirNuevoCertificado(operadorId: Long) {
        if (currentRole.value != RolUsuario.ADMIN) {
            showSnackbar("Solo el rol Administrador puede emitir certificados F 102.")
            return
        }
        viewModelScope.launch {
            val res = repository.emitirNuevoCertificado(operadorId, "Renovación F 102 emitida por Administrador")
            res.fold(
                onSuccess = { cert ->
                    showSnackbar("Certificado ${cert.numeroCertificado} emitido (previos marcados RELEVADO).")
                },
                onFailure = { showSnackbar(it.message ?: "No se pudo emitir el certificado.") },
            )
        }
    }

    fun suspenderCertificado(certificadoId: Long, motivo: String) {
        if (currentRole.value != RolUsuario.ADMIN) {
            showSnackbar("Solo el rol Administrador puede suspender certificados.")
            return
        }
        viewModelScope.launch {
            repository.suspenderCertificado(certificadoId, motivo)
            showSnackbar("Certificado suspendido.")
        }
    }

    fun reactivarCertificado(certificadoId: Long) {
        if (currentRole.value != RolUsuario.ADMIN) {
            showSnackbar("Solo el rol Administrador puede reactivar certificados.")
            return
        }
        viewModelScope.launch {
            repository.reactivarCertificado(certificadoId)
            showSnackbar("Certificado reactivado.")
        }
    }

    // --- Formulario F102 Actions (CU-1) ---
    fun updateForm(transform: (FormF102UiState) -> FormF102UiState) {
        _formState.update { current ->
            val updated = transform(current).copy(submitError = null)
            if (updated.cuit != current.cuit && !updated.personaExistente && updated.cuit.length >= 11) {
                val cuitErr = CuitValidator.validarCuit(updated.cuit).exceptionOrNull()?.message
                updated.copy(cuitError = cuitErr)
            } else if (updated.cuit.isBlank() || updated.personaExistente) {
                updated.copy(cuitError = null)
            } else {
                updated
            }
        }
    }

    fun onDepartamentoQueryChanged(text: String) {
        _formState.update { it.copy(departamento = text) }
        _depQuery.value = text
    }

    fun onLocalidadQueryChanged(text: String) {
        _formState.update { it.copy(localidad = text) }
        _locQuery.value = text
    }

    fun toggleCategoriaForm(id: String) {
        _formState.update { state ->
            val next = if (id in state.categorias) {
                state.categorias - id
            } else {
                state.categorias + id
            }
            state.copy(categorias = next)
        }
    }

    fun toggleActividadForm(codigo: String) {
        _formState.update { state ->
            val next = if (codigo in state.actividadesCodigos) {
                state.actividadesCodigos - codigo
            } else {
                state.actividadesCodigos + codigo
            }
            state.copy(actividadesCodigos = next)
        }
    }

    fun toggleCarneForm(carne: String) {
        _formState.update { state ->
            val next = if (carne in state.tiposCarne) {
                state.tiposCarne - carne
            } else {
                state.tiposCarne + carne
            }
            state.copy(tiposCarne = next)
        }
    }

    fun toggleRequisitoForm(codigo: String) {
        _formState.update { state ->
            val current = state.requisitosCumplidos[codigo] == true
            state.copy(
                requisitosCumplidos = state.requisitosCumplidos + (codigo to !current)
            )
        }
    }

    fun prefillSucursalParaCurrentPersona() {
        val persona = currentPersona.value
        if (persona != null) {
            prefillSucursalParaPersona(persona)
        } else {
            val user = _authState.value.usuario
            _formState.value = FormF102UiState(
                personaExistente = true,
                personaSeleccionadaId = user?.personaId,
                titular = user?.nombre.orEmpty(),
                email = user?.email.orEmpty(),
                departamento = "Capital",
                localidad = "Santiago del Estero",
            )
        }
    }

    fun prefillSucursalParaPersona(persona: PersonaEntity) {
        _formState.value = FormF102UiState(
            tipoPersona = if (persona.tipo == "PJ") "juridica" else "fisica",
            personaExistente = true,
            personaSeleccionadaId = persona.id,
            cuit = persona.identificador,
            titular = persona.nombre,
            dni = persona.dni.orEmpty(),
            telefono = persona.telefono.orEmpty(),
            email = persona.email.orEmpty(),
            departamento = "Capital",
            localidad = "Santiago del Estero",
        )
    }

    fun cargarDatosEjemplo() {
        val esPersona = currentRole.value == RolUsuario.PERSONA
        val persona = currentPersona.value
        if (esPersona && persona != null) {
            _formState.value = FormF102UiState(
                tipoPersona = if (persona.tipo == "PJ") "juridica" else "fisica",
                personaExistente = true,
                personaSeleccionadaId = persona.id,
                cuit = persona.identificador,
                titular = persona.nombre,
                dni = persona.dni.orEmpty(),
                telefono = persona.telefono ?: "(0385) 431-8910",
                email = persona.email ?: "titular@lanorita.com",
                nombreEstablecimiento = "SUCURSAL NUEVA ZONA SUR",
                departamento = "Capital",
                localidad = "Santiago del Estero",
                domicilio = "AV. SOLÍS N° 1420, B° EJÉRCITO ARGENTINO",
                habilitacionSanitaria = "Municipal en trámite • F 102",
                categorias = listOf("CARNICERIAS"),
                actividadesCodigos = listOf("922210"),
                tiposCarne = listOf("Bovina", "Porcina"),
                requisitosCumplidos = mapOf(
                    "habilitacion_municipal" to true,
                    "libreta_sanitaria" to true,
                    "afip" to true,
                ),
            )
        } else {
            _formState.value = FormF102UiState(
                tipoPersona = "fisica",
                personaExistente = false,
                cuit = "23-30917912-9",
                titular = "OLIVEIRA DANIEL ALBERTO",
                dni = "30.917.912",
                telefono = "(0385) 431-8910",
                email = "contacto@procarne.gob.ar",
                nombreEstablecimiento = "CARNICERÍA DEL NORTE",
                departamento = "Capital",
                localidad = "Santiago del Estero",
                domicilio = "AV. BELGRANO SUR N° 2450, B° RECONQUISTA",
                habilitacionSanitaria = "Municipal N° 528/2026 • ProCarne F 102",
                categorias = listOf("CARNICERIAS", "DISTRIBUIDOR"),
                actividadesCodigos = listOf("922210", "463110"),
                tiposCarne = listOf("Bovina", "Porcina", "Caprina (Cabrito SDE)"),
                requisitosCumplidos = mapOf(
                    "habilitacion_municipal" to true,
                    "libreta_sanitaria" to true,
                    "desinfeccion" to true,
                    "afip" to true,
                ),
            )
        }
    }

    fun resetFormulario() {
        val user = _authState.value.usuario
        if (user != null && RolUsuario.fromString(user.rol) == RolUsuario.PERSONA) {
            prefillSucursalParaCurrentPersona()
        } else {
            _formState.value = FormF102UiState()
        }
    }

    fun submitFormularioF102() {
        val f = _formState.value
        val role = currentRole.value
        if (!f.personaExistente) {
            val cuitCheck = CuitValidator.validarCuit(f.cuit)
            if (cuitCheck.isFailure) {
                _formState.update {
                    it.copy(
                        cuitError = cuitCheck.exceptionOrNull()?.message,
                        submitError = cuitCheck.exceptionOrNull()?.message,
                    )
                }
                return
            }
        }
        if (f.titular.isBlank() && !f.personaExistente) {
            _formState.update { it.copy(submitError = "Complete el Apellido y Nombre o Razón Social del titular.") }
            return
        }
        if (f.nombreEstablecimiento.isBlank()) {
            _formState.update { it.copy(submitError = "Complete el Nombre del Establecimiento / Fantasía.") }
            return
        }
        if (f.domicilio.isBlank()) {
            _formState.update { it.copy(submitError = "Complete el Domicilio del establecimiento.") }
            return
        }
        if (f.categorias.isEmpty()) {
            _formState.update { it.copy(submitError = "Seleccione al menos una categoría de actividad cárnica.") }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(submitting = true, submitError = null) }
            val res = repository.registrarOperador(
                input = NuevoOperadorInput(
                    tipoPersona = f.tipoPersona,
                    personaExistente = f.personaExistente,
                    personaSeleccionadaId = f.personaSeleccionadaId,
                    cuit = f.cuit,
                    titular = f.titular,
                    dni = f.dni,
                    telefono = f.telefono,
                    email = f.email,
                    nombreEstablecimiento = f.nombreEstablecimiento,
                    departamento = f.departamento,
                    localidad = f.localidad,
                    domicilio = f.domicilio,
                    habilitacionSanitaria = f.habilitacionSanitaria,
                    categorias = f.categorias,
                    actividadesCodigos = f.actividadesCodigos,
                    tiposCarne = f.tiposCarne,
                    requisitosCumplidos = f.requisitosCumplidos,
                ),
                callerRol = role,
            )
            res.fold(
                onSuccess = { op ->
                    _formState.update { FormF102UiState(lastCreatedOperador = op) }
                    if (role == RolUsuario.PERSONA) {
                        prefillSucursalParaCurrentPersona()
                        _formState.update { it.copy(lastCreatedOperador = op) }
                    }
                    val msg = if (op.estadoRegistral == EstadoRegistral.ACTIVO.name) {
                        "Operador habilitado — Cartón F 102 N° ${op.numeroCertificadoF102}."
                    } else {
                        "Sucursal registrada en estado PENDIENTE de autorización."
                    }
                    showSnackbar(msg)
                },
                onFailure = { err ->
                    _formState.update {
                        it.copy(submitting = false, submitError = err.message ?: "Error al registrar.")
                    }
                },
            )
        }
    }

    fun clearLastCreatedOperador() {
        _formState.update { it.copy(lastCreatedOperador = null) }
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
