package com.example.data.remote

import com.example.domain.RoccaCatalogos
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@Serializable
data class GeorefRef(
    val id: String? = null,
    val nombre: String? = null,
)

@Serializable
data class GeorefLocalidad(
    val id: String,
    val nombre: String,
    val provincia: GeorefRef? = null,
    val departamento: GeorefRef? = null,
)

@Serializable
data class GeorefDepartamento(
    val id: String,
    val nombre: String,
    @SerialName("nombre_completo") val nombreCompleto: String? = null,
    val provincia: GeorefRef? = null,
)

@Serializable
data class LocalidadesResponse(
    val cantidad: Int = 0,
    val localidades: List<GeorefLocalidad> = emptyList(),
)

@Serializable
data class DepartamentosResponse(
    val cantidad: Int = 0,
    val departamentos: List<GeorefDepartamento> = emptyList(),
)

data class GeorefSuggestion(
    val nombre: String,
    val subtitulo: String,
)

interface GeorefApi {
    @GET("localidades")
    suspend fun buscarLocalidades(
        @Query("nombre") nombre: String,
        @Query("provincia") provincia: String? = "Santiago del Estero",
        @Query("max") max: Int = 15,
    ): LocalidadesResponse

    @GET("departamentos")
    suspend fun buscarDepartamentos(
        @Query("nombre") nombre: String,
        @Query("provincia") provincia: String? = "Santiago del Estero",
        @Query("max") max: Int = 15,
    ): DepartamentosResponse
}

class GeorefClient {
    private val json = Json { ignoreUnknownKeys = true }

    private val api: GeorefApi by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(6, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl("https://apis.datos.gob.ar/georef/api/v2.0/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GeorefApi::class.java)
    }

    suspend fun buscarDepartamentos(query: String): List<GeorefSuggestion> {
        val clean = query.trim()
        if (clean.length < 2) {
            return RoccaCatalogos.DEPARTAMENTOS_SDE_FALLBACK.take(6).map {
                GeorefSuggestion(nombre = it, subtitulo = "Santiago del Estero")
            }
        }
        return try {
            val resp = api.buscarDepartamentos(nombre = clean, provincia = "Santiago del Estero")
            if (resp.departamentos.isNotEmpty()) {
                resp.departamentos.map { d ->
                    GeorefSuggestion(
                        nombre = d.nombre,
                        subtitulo = d.provincia?.nombre ?: "Santiago del Estero",
                    )
                }
            } else {
                fallbackDepartamentos(clean)
            }
        } catch (_: Exception) {
            fallbackDepartamentos(clean)
        }
    }

    suspend fun buscarLocalidades(query: String, departamento: String? = null): List<GeorefSuggestion> {
        val clean = query.trim()
        if (clean.length < 2) {
            return RoccaCatalogos.LOCALIDADES_SDE_FALLBACK.take(6).map {
                GeorefSuggestion(nombre = it, subtitulo = "Santiago del Estero")
            }
        }
        return try {
            val resp = api.buscarLocalidades(nombre = clean, provincia = "Santiago del Estero")
            if (resp.localidades.isNotEmpty()) {
                val filtered = if (!departamento.isNullOrBlank()) {
                    val matching = resp.localidades.filter {
                        it.departamento?.nombre?.contains(departamento, ignoreCase = true) == true
                    }
                    matching.ifEmpty { resp.localidades }
                } else {
                    resp.localidades
                }
                filtered.map { l ->
                    val sub = listOfNotNull(l.departamento?.nombre, l.provincia?.nombre)
                        .joinToString(" — ")
                        .ifBlank { "Santiago del Estero" }
                    GeorefSuggestion(nombre = l.nombre, subtitulo = sub)
                }
            } else {
                fallbackLocalidades(clean)
            }
        } catch (_: Exception) {
            fallbackLocalidades(clean)
        }
    }

    private fun fallbackDepartamentos(query: String): List<GeorefSuggestion> =
        RoccaCatalogos.DEPARTAMENTOS_SDE_FALLBACK
            .filter { it.contains(query, ignoreCase = true) }
            .map { GeorefSuggestion(nombre = it, subtitulo = "Santiago del Estero (Catálogo local)") }

    private fun fallbackLocalidades(query: String): List<GeorefSuggestion> =
        RoccaCatalogos.LOCALIDADES_SDE_FALLBACK
            .filter { it.contains(query, ignoreCase = true) }
            .map { GeorefSuggestion(nombre = it, subtitulo = "Santiago del Estero (Catálogo local)") }
}
