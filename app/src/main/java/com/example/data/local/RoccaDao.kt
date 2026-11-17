package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RoccaDao {

    // --- Usuarios ---
    @Query("SELECT * FROM usuarios WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUsuarioByEmail(email: String): UsuarioEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUsuario(usuario: UsuarioEntity): Long

    @Query("UPDATE usuarios SET rol = :rol WHERE LOWER(email) = LOWER(:email)")
    suspend fun updateUsuarioRol(email: String, rol: String)

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun countUsuarios(): Int

    // --- Personas ---
    @Query("SELECT * FROM personas ORDER BY nombre ASC")
    fun observePersonas(): Flow<List<PersonaEntity>>

    @Query("SELECT * FROM personas WHERE id = :id LIMIT 1")
    fun observePersonaById(id: Long): Flow<PersonaEntity?>

    @Query("SELECT * FROM personas WHERE id = :id LIMIT 1")
    suspend fun getPersonaById(id: Long): PersonaEntity?

    @Query("SELECT * FROM personas WHERE identificador = :identificador LIMIT 1")
    suspend fun getPersonaByIdentificador(identificador: String): PersonaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersona(persona: PersonaEntity): Long

    @Update
    suspend fun updatePersona(persona: PersonaEntity)

    // --- Adjuntos de Persona (CU-4) ---
    @Query("SELECT * FROM adjuntos_persona WHERE personaId = :personaId ORDER BY id DESC")
    fun observeAdjuntosForPersona(personaId: Long): Flow<List<AdjuntoPersonaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdjuntoPersona(adjunto: AdjuntoPersonaEntity): Long

    @Query("DELETE FROM adjuntos_persona WHERE id = :adjuntoId")
    suspend fun deleteAdjuntoPersona(adjuntoId: Long)

    // --- Operadores ---
    @Query("SELECT * FROM operadores ORDER BY id DESC")
    fun observeOperadores(): Flow<List<OperadorEntity>>

    @Query("SELECT * FROM operadores WHERE id = :id LIMIT 1")
    fun observeOperadorById(id: Long): Flow<OperadorEntity?>

    @Query("SELECT * FROM operadores WHERE id = :id LIMIT 1")
    suspend fun getOperadorById(id: Long): OperadorEntity?

    @Query("SELECT COUNT(*) FROM operadores")
    suspend fun countOperadores(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperador(operador: OperadorEntity): Long

    @Query("UPDATE operadores SET estadoRegistral = :estado, bajaFecha = :bajaFecha WHERE id = :id")
    suspend fun updateEstadoOperador(id: Long, estado: String, bajaFecha: String?)

    // --- Requisitos ---
    @Query("SELECT * FROM requisitos_inscripcion ORDER BY esGeneral DESC, id ASC")
    fun observeAllRequisitos(): Flow<List<RequisitoEntity>>

    @Query("SELECT * FROM requisitos_inscripcion WHERE operadorId = :operadorId ORDER BY esGeneral DESC, id ASC")
    fun observeRequisitosForOperador(operadorId: Long): Flow<List<RequisitoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequisitos(requisitos: List<RequisitoEntity>)

    @Query("UPDATE requisitos_inscripcion SET cumplido = :cumplido, observaciones = :observaciones WHERE id = :requisitoId")
    suspend fun updateRequisitoCumplimiento(requisitoId: Long, cumplido: Boolean, observaciones: String?)

    // --- Adjuntos de Operador (CU-3) ---
    @Query("SELECT * FROM adjuntos_operador ORDER BY id DESC")
    fun observeAllAdjuntos(): Flow<List<AdjuntoEntity>>

    @Query("SELECT * FROM adjuntos_operador WHERE operadorId = :operadorId ORDER BY id DESC")
    fun observeAdjuntosForOperador(operadorId: Long): Flow<List<AdjuntoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdjunto(adjunto: AdjuntoEntity): Long

    @Query("DELETE FROM adjuntos_operador WHERE id = :adjuntoId")
    suspend fun deleteAdjunto(adjuntoId: Long)

    // --- Certificados (CU-7) ---
    @Query("SELECT * FROM certificados ORDER BY id DESC")
    fun observeAllCertificados(): Flow<List<CertificadoEntity>>

    @Query("SELECT * FROM certificados WHERE operadorId = :operadorId ORDER BY id DESC")
    fun observeCertificadosForOperador(operadorId: Long): Flow<List<CertificadoEntity>>

    @Query("SELECT * FROM certificados WHERE operadorId = :operadorId ORDER BY id DESC")
    suspend fun getCertificadosForOperador(operadorId: Long): List<CertificadoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificado(certificado: CertificadoEntity): Long

    @Query("UPDATE certificados SET estado = :estado, observaciones = :observaciones, motivoSuspension = :motivoSuspension WHERE id = :certificadoId")
    suspend fun updateEstadoCertificado(
        certificadoId: Long,
        estado: String,
        observaciones: String?,
        motivoSuspension: String?,
    )
}
