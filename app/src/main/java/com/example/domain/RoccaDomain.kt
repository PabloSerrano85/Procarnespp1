package com.example.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Dominio de ROCCA (Módulo I: Registro de Operadores — CU-1 a CU-10).
 */

enum class RolUsuario(
    val key: String,
    val label: String,
    val descripcion: String,
) {
    PERSONA(
        key = "persona",
        label = "Persona (Titular)",
        descripcion = "Administra sus sucursales, sube requisitos/adjuntos y consulta vencimientos.",
    ),
    SUPERVISOR(
        key = "supervisor",
        label = "Supervisor",
        descripcion = "Fiscaliza el padrón, verifica requisitos y gestiona el ciclo de vida.",
    ),
    ADMIN(
        key = "admin",
        label = "Administrador",
        descripcion = "Acceso total al padrón, autoriza operadores y emite certificados F 102.",
    ),
    ;

    companion object {
        fun fromString(raw: String?): RolUsuario {
            val clean = raw?.trim()?.lowercase().orEmpty()
            return when {
                clean.contains("admin") -> ADMIN
                clean.contains("super") -> SUPERVISOR
                else -> PERSONA
            }
        }
    }
}

enum class TipoAdjuntoPersona(
    val codigo: String,
    val label: String,
    val descripcion: String,
) {
    DNI_FRENTE("dni_frente", "DNI Frente", "Foto o PDF del frente del DNI del titular"),
    DNI_DORSO("dni_dorso", "DNI Dorso", "Foto o PDF del dorso del DNI del titular"),
    CUIT("cuit", "Constancia CUIT", "Constancia de inscripción AFIP / ARCA vigente"),
    IIBB("iibb", "Inscripción IIBB", "Constancia Ingresos Brutos / DGR Santiago del Estero"),
    ;

    companion object {
        fun fromCodigo(codigo: String): TipoAdjuntoPersona? =
            entries.firstOrNull { it.codigo.equals(codigo, ignoreCase = true) }
    }
}

data class CategoriaCarnica(
    val id: String,
    val nombre: String,
    val codigoAfip: String,
    val descripcion: String,
    val especiesSugeridas: String,
)

data class RequisitoCatalogo(
    val codigo: String,
    val nombre: String,
    val obligatorio: Boolean,
    val esGeneral: Boolean = false,
    val categoriaId: String? = null,
)

data class DomicilioData(
    val calle: String,
    val numero: String? = null,
    val departamento: String = "",
    val localidad: String = "",
    val provincia: String = "Santiago del Estero",
    val codigoPostal: String? = null,
)

// --- CU-8: Catálogo jerárquico de Actividades Económicas ---
data class GrupoActividad(
    val codigo: String,
    val nombre: String,
)

data class SubgrupoActividad(
    val grupoCodigo: String,
    val codigo: String,
    val nombre: String,
)

data class SubsubgrupoActividad(
    val grupoCodigo: String,
    val subgrupoCodigo: String,
    val codigo: String,
    val nombre: String,
)

data class ActividadEconomica(
    val codigo: String,
    val nombre: String,
    val grupoCodigo: String,
    val subgrupoCodigo: String,
    val subsubgrupoCodigo: String,
)

enum class EstadoRegistral(val labelUi: String) {
    ACTIVO("Habilitado"),
    PENDIENTE("Pendiente"),
    SUSPENDIDO("Suspendido"),
    BAJADO("Dado de baja"),
    ;

    companion object {
        fun fromString(raw: String): EstadoRegistral {
            val upper = raw.uppercase()
            return entries.firstOrNull { it.name == upper } ?: when {
                upper.contains("ACTIVO") || upper.contains("HABILITADO") || upper.contains("ALTA") -> ACTIVO
                upper.contains("PEND") || upper.contains("RENOV") || upper.contains("VENC") -> PENDIENTE
                upper.contains("SUSP") -> SUSPENDIDO
                else -> BAJADO
            }
        }
    }
}

enum class EstadoCertificado(val labelUi: String) {
    ACTIVO("Vigente"),
    VENCIDO("Vencido"),
    SUSPENDIDO("Suspendido"),
    RELEVADO("Relevado"),
}

object RoccaCatalogos {

    val CATEGORIAS_CARNICAS = listOf(
        CategoriaCarnica(
            id = "CARNICERIAS",
            nombre = "Carnicería Minorista",
            codigoAfip = "922210",
            descripcion = "Venta al por menor de carnes rojas, menudencias y chacinados frescos.",
            especiesSugeridas = "Bovina, Porcina",
        ),
        CategoriaCarnica(
            id = "SALA_DE_DESPOSTE",
            nombre = "Despostadero / Ciclo II",
            codigoAfip = "101120",
            descripcion = "Despostado, troceo y deshuese de reses vacunas y porcinos.",
            especiesSugeridas = "Bovina, Porcina, Caprina",
        ),
        CategoriaCarnica(
            id = "FRIGORIFICO",
            nombre = "Frigorífico / Ciclo I",
            codigoAfip = "101111",
            descripcion = "Matanza y faena de ganado bovino, porcino y aviar en planta propia.",
            especiesSugeridas = "Bovina, Porcina, Aviar",
        ),
        CategoriaCarnica(
            id = "MATARIFE",
            nombre = "Matarife Abastecedor",
            codigoAfip = "463120",
            descripcion = "Comercialización mayorista de ganado y carne con faena en plantas autorizadas.",
            especiesSugeridas = "Bovina, Porcina",
        ),
        CategoriaCarnica(
            id = "DISTRIBUIDOR",
            nombre = "Distribuidor Mayorista",
            codigoAfip = "463110",
            descripcion = "Distribución mayorista de medias reses, cortes y cortes envasados.",
            especiesSugeridas = "Bovina, Porcina",
        ),
        CategoriaCarnica(
            id = "TRANSPONEDOR",
            nombre = "Transportista de Carnes",
            codigoAfip = "492110",
            descripcion = "Transporte interjurisdiccional y local de carnes frescas en furgón térmico.",
            especiesSugeridas = "Bovina, Caprina",
        ),
    )

    val TIPOS_CARNE = listOf(
        "Bovina",
        "Porcina",
        "Caprina (Cabrito SDE)",
        "Ovino",
        "Aviar",
        "Menudencias/Chacinados",
    )

    val REQUISITOS_FORMULARIO = listOf(
        RequisitoCatalogo(
            codigo = "habilitacion_municipal",
            nombre = "Habilitación Municipal del Local Comercial",
            obligatorio = true,
            categoriaId = "CARNICERIAS",
        ),
        RequisitoCatalogo(
            codigo = "libreta_sanitaria",
            nombre = "Libreta Sanitaria del personal expedida por autoridad competente",
            obligatorio = true,
            categoriaId = "CARNICERIAS",
        ),
        RequisitoCatalogo(
            codigo = "desinfeccion",
            nombre = "Certificado de Desinfección y Control de Plagas vigente",
            obligatorio = true,
            categoriaId = "CARNICERIAS",
        ),
        RequisitoCatalogo(
            codigo = "afip",
            nombre = "Constancia de Inscripción en AFIP / ARCA y DGR Santiago del Estero",
            obligatorio = true,
            esGeneral = true,
            categoriaId = "CARNICERIAS",
        ),
        RequisitoCatalogo(
            codigo = "croquis",
            nombre = "Croquis o plano de instalaciones de frío y mesas de acero inoxidable",
            obligatorio = false,
            categoriaId = "CARNICERIAS",
        ),
        RequisitoCatalogo(
            codigo = "memoria_operativa",
            nombre = "Memoria Constructiva y Operativa de Planta / Desposte",
            obligatorio = true,
            categoriaId = "SALA_DE_DESPOSTE",
        ),
        RequisitoCatalogo(
            codigo = "certificado_efluentes",
            nombre = "Certificado de Tratamiento y Vuelco de Efluentes",
            obligatorio = true,
            categoriaId = "FRIGORIFICO",
        ),
        RequisitoCatalogo(
            codigo = "matricula_ruca",
            nombre = "Matrícula RUCA vigente y habilitación sanitaria de transporte/cámara",
            obligatorio = true,
            categoriaId = "TRANSPONEDOR",
        ),
    )

    // --- CU-8: Catálogo jerárquico de Actividades Económicas ---
    val GRUPOS_ACTIVIDAD = listOf(
        GrupoActividad("C", "Industria Manufacturera Cárnica"),
        GrupoActividad("G", "Comercio al por Mayor y Menor de Carnes"),
        GrupoActividad("H", "Transporte y Logística Refrigerada"),
        GrupoActividad("A", "Producción Primaria y Faena en Origen"),
    )

    val SUBGRUPOS_ACTIVIDAD = listOf(
        SubgrupoActividad("C", "101", "Elaboración y conservación de carne y productos cárnicos"),
        SubgrupoActividad("G", "463", "Venta al por mayor de carnes, ganado y subproductos"),
        SubgrupoActividad("G", "472", "Venta al por menor de carnes rojas, aves y chacinados"),
        SubgrupoActividad("H", "492", "Transporte automotor de cargas refrigeradas"),
        SubgrupoActividad("A", "014", "Cría de ganado y faena controlada en origen"),
    )

    val SUBSUBGRUPOS_ACTIVIDAD = listOf(
        SubsubgrupoActividad("C", "101", "1011", "Matanza, faena y procesamiento industrial (Ciclo I y II)"),
        SubsubgrupoActividad("G", "463", "4631", "Abastecimiento mayorista y matarifes"),
        SubsubgrupoActividad("G", "472", "4721", "Carnicerías minoristas y locales de expendio"),
        SubsubgrupoActividad("H", "492", "4921", "Flota térmica habilitada para reses y cortes"),
        SubsubgrupoActividad("A", "014", "0141", "Cría bovina, porcina y caprina provincial"),
    )

    val ACTIVIDADES_ECONOMICAS = listOf(
        ActividadEconomica("101111", "Matanza de ganado bovino y procesamiento en frigorífico (Ciclo I)", "C", "101", "1011"),
        ActividadEconomica("101120", "Procesamiento, troceo y desposte de carne (Ciclo II)", "C", "101", "1011"),
        ActividadEconomica("101130", "Elaboración de chacinados, embutidos y fiambres frescos", "C", "101", "1011"),
        ActividadEconomica("463110", "Venta al por mayor de carnes rojas y menudencias (Distribuidor)", "G", "463", "4631"),
        ActividadEconomica("463120", "Comercialización mayorista como Matarife Abastecedor", "G", "463", "4631"),
        ActividadEconomica("922210", "Venta al por menor de carnes rojas, menudencias y chacinados frescos (Carnicería)", "G", "472", "4721"),
        ActividadEconomica("472120", "Venta al por menor de carnes de ave y huevos", "G", "472", "4721"),
        ActividadEconomica("492110", "Transporte automotor de carnes en furgón térmico habilitado", "H", "492", "4921"),
        ActividadEconomica("014113", "Cría de ganado bovino y faena en origen autorizada", "A", "014", "0141"),
    )

    val DEPARTAMENTOS_SDE_FALLBACK = listOf(
        "Capital",
        "Banda",
        "Río Hondo",
        "Robles",
        "Choya",
        "General Taboada",
        "Moreno",
        "Avellaneda",
        "Belgrano",
        "Copo",
        "Alberdi",
        "Jiménez",
        "Loreto",
        "Ojo de Agua",
        "Pellegrini",
        "Quebrachos",
        "Salavina",
        "San Martín",
        "Sarmiento",
        "Silípica",
    )

    val LOCALIDADES_SDE_FALLBACK = listOf(
        "Santiago del Estero",
        "La Banda",
        "Termas de Río Hondo",
        "Frías",
        "Añatuya",
        "Fernández",
        "Quimilí",
        "Monte Quemado",
        "Loreto",
        "Clodomira",
        "Suncho Corral",
        "Bandera",
        "Campo Gallo",
        "Selva",
        "Villa Ojo de Agua",
    )

    fun nombreCategoria(categoriaId: String?): String {
        if (categoriaId.isNullOrBlank()) return "—"
        return when (categoriaId.uppercase()) {
            "CARNICERIAS" -> "Carnicería Minorista"
            "CAMARAS_FRIGORIFICAS", "FRIGORIFICO" -> "Frigorífico / Ciclo I"
            "SALA_DE_DESPOSTE" -> "Despostadero / Ciclo II"
            "FABRICA_DE_CHACINADOS", "MATARIFE" -> "Matarife Abastecedor"
            "DISTRIBUIDOR" -> "Distribuidor Mayorista"
            "TRANSPONEDOR" -> "Transportista de Carnes"
            "SUPERMERCADO" -> "Carnicería / Supermercado"
            else -> categoriaId
        }
    }

    fun codigoAfipParaCategoria(categoriaId: String?): String {
        return CATEGORIAS_CARNICAS.firstOrNull { it.id == categoriaId }?.codigoAfip ?: "922210"
    }

    fun especiesParaCategoria(categoriaId: String?): String {
        return CATEGORIAS_CARNICAS.firstOrNull { it.id == categoriaId }?.especiesSugeridas ?: "Bovina"
    }

    fun requisitosParaCategorias(categorias: List<String>): List<RequisitoCatalogo> {
        if (categorias.isEmpty()) {
            return REQUISITOS_FORMULARIO.filter { it.categoriaId == "CARNICERIAS" || it.esGeneral }
        }
        return REQUISITOS_FORMULARIO.filter { req ->
            req.esGeneral || req.categoriaId == "CARNICERIAS" || req.categoriaId in categorias
        }
    }

    fun formatearDomicilio(
        calle: String?,
        numero: String?,
        localidad: String?,
        provincia: String?,
    ): String {
        val c = calle?.trim().orEmpty()
        if (c.isEmpty() && localidad.isNullOrBlank()) return ""
        val calleParte = buildString {
            append(c)
            if (!numero.isNullOrBlank()) {
                if (isNotEmpty()) append(" ")
                append("N°${numero.trim()}")
            }
        }
        val loc = localidad?.trim().orEmpty()
        val prov = provincia?.trim().orEmpty()
        val resto = listOf(loc, prov).filter { it.isNotEmpty() }.joinToString(", ")
        return listOf(calleParte, resto).filter { it.isNotEmpty() }.joinToString(" - ")
    }

    fun diasParaVencimiento(isoDate: String?): Long? {
        if (isoDate.isNullOrBlank()) return null
        return try {
            val clean = isoDate.take(10)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val target = sdf.parse(clean) ?: return null
            val todayStr = sdf.format(Date())
            val today = sdf.parse(todayStr) ?: Date()
            val diffMs = target.time - today.time
            TimeUnit.MILLISECONDS.toDays(diffMs)
        } catch (_: Exception) {
            null
        }
    }

    fun formatearFechaCorta(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return "—"
        return try {
            val clean = isoDate.take(10)
            val parts = clean.split("-")
            if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else clean
        } catch (_: Exception) {
            isoDate.take(10)
        }
    }

    /**
     * Port directo de `parsearDomicilio` en `src/frontend/src/types.ts`.
     */
    fun parsearDomicilio(
        texto: String,
        departamento: String = "",
        localidadFallback: String = "",
    ): DomicilioData {
        val trimmed = texto.trim()
        if (trimmed.isEmpty()) {
            return DomicilioData(
                calle = "",
                numero = null,
                departamento = departamento,
                localidad = localidadFallback.ifBlank { "Santiago del Estero" },
                provincia = "Santiago del Estero",
            )
        }
        val idxSep = trimmed.lastIndexOf(" - ")
        val parteCalle = if (idxSep != -1) trimmed.substring(0, idxSep) else trimmed
        val parteResto = if (idxSep != -1) trimmed.substring(idxSep + 3) else ""

        var localidad = localidadFallback
        var provincia = "Santiago del Estero"
        if (parteResto.contains(", ")) {
            val idxComa = parteResto.lastIndexOf(", ")
            localidad = parteResto.substring(0, idxComa).trim().ifEmpty { localidadFallback }
            provincia = parteResto.substring(idxComa + 2).trim().ifEmpty { "Santiago del Estero" }
        } else if (parteResto.isNotBlank()) {
            localidad = parteResto.trim()
        }

        val regexNum = Regex("""N[°º.]?\s*(\d+[\d/]*)""", RegexOption.IGNORE_CASE)
        val match = regexNum.find(parteCalle)
        val numero = match?.groupValues?.getOrNull(1)
        val calle = if (match != null) {
            parteCalle.substring(0, match.range.first).trimEnd(' ', '-').ifBlank { parteCalle }
        } else {
            parteCalle.trim()
        }

        return DomicilioData(
            calle = calle,
            numero = numero,
            departamento = departamento,
            localidad = localidad.ifBlank { "Santiago del Estero" },
            provincia = provincia,
        )
    }
}
