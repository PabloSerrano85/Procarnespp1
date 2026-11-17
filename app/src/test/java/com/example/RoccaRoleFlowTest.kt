package com.example

import android.content.Context
import androidx.room.Room
import com.example.data.local.RoccaDatabase
import com.example.data.local.SessionPreferences
import com.example.data.repository.NuevoOperadorInput
import com.example.data.repository.RoccaRepository
import com.example.domain.EstadoCertificado
import com.example.domain.EstadoRegistral
import com.example.domain.RolUsuario
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RoccaRoleFlowTest {

    private lateinit var db: RoccaDatabase
    private lateinit var repo: RoccaRepository

    @Before
    fun setup() {
        val context: Context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, RoccaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = RoccaRepository(db.roccaDao(), SessionPreferences(context))
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `seed incluye los 3 roles asignados y flujo Persona a Admin F102`() = runBlocking {
        repo.ensureSeeded()

        val personaDemo = repo.loginAsDemoRole(RolUsuario.PERSONA).getOrThrow()
        assertEquals(RolUsuario.PERSONA.key, personaDemo.rol)

        val supervisorDemo = repo.loginAsDemoRole(RolUsuario.SUPERVISOR).getOrThrow()
        assertEquals(RolUsuario.SUPERVISOR.key, supervisorDemo.rol)

        val adminDemo = repo.loginAsDemoRole(RolUsuario.ADMIN).getOrThrow()
        assertEquals(RolUsuario.ADMIN.key, adminDemo.rol)

        // Signup con CUIT genera entidad Persona
        val signupUser = repo.signup(
            nombre = "Carnicería Don Juan",
            tipoDocumento = "CUIT",
            documento = "23-30917912-9",
            email = "donjuan@carniceria.com",
            password = "Password2026!",
            rol = RolUsuario.PERSONA,
        ).getOrThrow()
        assertNotNull(signupUser.personaId)

        // Persona da de alta sucursal -> inicia PENDIENTE sin certificado activo
        val nuevaSucursal = repo.registrarOperador(
            input = NuevoOperadorInput(
                tipoPersona = "fisica",
                personaExistente = true,
                personaSeleccionadaId = signupUser.personaId,
                cuit = "23-30917912-9",
                titular = "Carnicería Don Juan",
                dni = "",
                telefono = "3854123456",
                email = "donjuan@carniceria.com",
                nombreEstablecimiento = "Sucursal Centro",
                departamento = "Capital",
                localidad = "Santiago del Estero",
                domicilio = "Av. Belgrano N° 100",
                habilitacionSanitaria = "Mun 12/2026",
                categorias = listOf("CARNICERIAS"),
                actividadesCodigos = listOf("922210"),
                tiposCarne = listOf("Bovina"),
                requisitosCumplidos = mapOf("habilitacion_municipal" to true),
            ),
            callerRol = RolUsuario.PERSONA,
        ).getOrThrow()

        assertEquals(EstadoRegistral.PENDIENTE.name, nuevaSucursal.estadoRegistral)
        assertTrue(repo.observeCertificados(nuevaSucursal.id).first().isEmpty())

        // CU-4: Adjunto personal de Persona
        repo.agregarAdjuntoPersona(
            personaId = signupUser.personaId!!,
            tipoDocumento = "dni_frente",
            filename = "dni_juan.jpg",
            contentType = "image/jpeg",
            uriString = "content://test/dni_juan.jpg",
            sizeBytes = 1024L,
        )
        assertEquals(1, repo.observeAdjuntosPersona(signupUser.personaId!!).first().size)

        // Administrador autoriza y emite Certificado F 102 (CU-7)
        val cert = repo.autorizarYEmitirCertificado(nuevaSucursal.id).getOrThrow()
        assertEquals(EstadoCertificado.ACTIVO.name, cert.estado)
        assertNotNull(cert.fechaVencimiento)

        val opActualizado = db.roccaDao().getOperadorById(nuevaSucursal.id)!!
        assertEquals(EstadoRegistral.ACTIVO.name, opActualizado.estadoRegistral)
    }
}
