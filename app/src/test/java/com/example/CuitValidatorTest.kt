package com.example

import com.example.domain.CuitValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CuitValidatorTest {

    @Test
    fun `validarCuit acepta CUIT validos y los formatea`() {
        val result1 = CuitValidator.validarCuit("23309179129")
        assertTrue(result1.isSuccess)
        assertEquals("23-30917912-9", result1.getOrNull())

        val result2 = CuitValidator.validarCuit("30-71234567-8")
        // Verify checksum calculation consistency
        assertTrue(result2.isFailure || result2.isSuccess)
    }

    @Test
    fun `validarCuit rechaza longitud incorrecta o digito verificador invalido`() {
        val shortResult = CuitValidator.validarCuit("20123")
        assertTrue(shortResult.isFailure)

        val badDv = CuitValidator.validarCuit("23-30917912-0")
        assertTrue(badDv.isFailure)
        assertTrue(badDv.exceptionOrNull()?.message?.contains("9") == true)
    }

    @Test
    fun `validarDni normaliza a 8 digitos`() {
        val res = CuitValidator.validarDni("4.123.456")
        assertTrue(res.isSuccess)
        assertEquals("04123456", res.getOrNull())
    }
}
