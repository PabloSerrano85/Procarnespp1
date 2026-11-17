package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.RoccaCatalogos

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["email"], unique = true)]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val passwordHash: String,
    val nombre: String,
    val rol: String = "persona", // "persona" | "supervisor" | "admin"
    val personaId: Long? = null,
    val creadoEn: String = "",
)

@Entity(
    tableName = "personas",
    indices = [Index(value = ["tipoDocumento", "identificador"], unique = true)]
)
data class PersonaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipo: String, // "PF" | "PJ"
    val tipoDocumento: String, // "CUIT" | "DNI"
    val identificador: String,
    val nombre: String,
    val dni: String? = null,
    val telefono: String? = null,
    val email: String? = null,
)

@Entity(
    tableName = "adjuntos_persona",
    foreignKeys = [
        ForeignKey(
            entity = PersonaEntity::class,
            parentColumns = ["id"],
            childColumns = ["personaId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index(value = ["personaId"])]
)
data class AdjuntoPersonaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personaId: Long,
    val tipoDocumento: String, // "dni_frente" | "dni_dorso" | "cuit" | "iibb"
    val filename: String,
    val contentType: String,
    val uriString: String,
    val sizeBytes: Long = 0L,
    val creadoEn: String = "",
)

@Entity(
    tableName = "operadores",
    foreignKeys = [
        ForeignKey(
            entity = PersonaEntity::class,
            parentColumns = ["id"],
            childColumns = ["personaId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index(value = ["personaId"])]
)
data class OperadorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personaId: Long,
    val tipo: String,
    val razonSocial: String,
    val nombreEstablecimiento: String,
    val cuit: String,
    val titular: String,
    val calle: String,
    val numero: String? = null,
    val departamento: String = "",
    val localidad: String = "",
    val provincia: String = "Santiago del Estero",
    val codigoPostal: String? = null,
    val telefono: String? = null,
    val email: String? = null,
    val categoria: String,
    val categoriasCsv: String = "",
    val codigoAfipCsv: String = "",
    val actividadesCodigosCsv: String = "",
    val tiposCarneCsv: String = "",
    val habilitacionSanitaria: String? = null,
    val estadoRegistral: String = "PENDIENTE",
    val altaFecha: String? = null,
    val bajaFecha: String? = null,
    val creadoEn: String = "",
) {
    val numeroCertificadoF102: String
        get() = (100000L + id).toString()

    val domicilioFormateado: String
        get() = RoccaCatalogos.formatearDomicilio(calle, numero, localidad, provincia)

    val categoriasLista: List<String>
        get() = categoriasCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            .ifEmpty { listOf(categoria) }

    val actividadesLista: List<String>
        get() = actividadesCodigosCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    val tiposCarneLista: List<String>
        get() = tiposCarneCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

@Entity(
    tableName = "requisitos_inscripcion",
    foreignKeys = [
        ForeignKey(
            entity = OperadorEntity::class,
            parentColumns = ["id"],
            childColumns = ["operadorId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index(value = ["operadorId"])]
)
data class RequisitoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operadorId: Long,
    val codigo: String,
    val nombre: String,
    val esGeneral: Boolean,
    val obligatorio: Boolean,
    val cumplido: Boolean = false,
    val observaciones: String? = null,
)

@Entity(
    tableName = "adjuntos_operador",
    foreignKeys = [
        ForeignKey(
            entity = OperadorEntity::class,
            parentColumns = ["id"],
            childColumns = ["operadorId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index(value = ["operadorId"])]
)
data class AdjuntoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operadorId: Long,
    val requisitoCodigo: String,
    val filename: String,
    val contentType: String,
    val uriString: String,
    val sizeBytes: Long = 0L,
    val creadoEn: String = "",
)

@Entity(
    tableName = "certificados",
    foreignKeys = [
        ForeignKey(
            entity = OperadorEntity::class,
            parentColumns = ["id"],
            childColumns = ["operadorId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index(value = ["operadorId"])]
)
data class CertificadoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operadorId: Long,
    val numeroCertificado: String,
    val tipoCertificado: String = "HABILITACION",
    val fechaEmision: String,
    val fechaVencimiento: String? = null,
    val actividadesEconomicas: String = "",
    val estado: String = "ACTIVO",
    val observaciones: String? = null,
    val motivoSuspension: String? = null,
    val creadoEn: String = "",
)
