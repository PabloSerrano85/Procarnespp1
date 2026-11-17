package com.example.domain

/**
 * Validación y formateo de CUIT / DNI argentino (padrón AFIP / ARCA).
 * Basado en `src/backend/app/core/cuit/validator.py`.
 */
object CuitValidator {
    private val PESOS = intArrayOf(5, 4, 3, 2, 7, 6, 5, 4, 3, 2)

    fun soloDigitos(input: String): String = input.filter { it.isDigit() }

    /**
     * Valida formato de CUIT: exactamente 11 dígitos + dígito verificador Módulo 11.
     * Retorna Pair(cuitNormalizadoFormateado, errorMessage?).
     */
    fun validarCuit(raw: String): Result<String> {
        val digitos = soloDigitos(raw)
        if (digitos.length != 11) {
            return Result.failure(IllegalArgumentException("El CUIT debe tener 11 dígitos."))
        }
        var total = 0
        for (i in 0 until 10) {
            total += (digitos[i] - '0') * PESOS[i]
        }
        val mod = total % 11
        var dv = 11 - mod
        if (dv == 11) dv = 0
        if (dv == 10) dv = 9

        val actualDv = digitos[10] - '0'
        if (actualDv != dv) {
            return Result.failure(
                IllegalArgumentException(
                    "El CUIT no es válido: el dígito verificador debería ser $dv."
                )
            )
        }
        return Result.success(formatearCuit(digitos))
    }

    fun validarDni(raw: String): Result<String> {
        val digitos = soloDigitos(raw)
        if (digitos.length !in 7..8) {
            return Result.failure(IllegalArgumentException("El DNI debe tener 8 dígitos."))
        }
        return Result.success(digitos.padStart(8, '0'))
    }

    fun formatearCuit(raw: String): String {
        val d = soloDigitos(raw)
        return if (d.length == 11) {
            "${d.substring(0, 2)}-${d.substring(2, 10)}-${d.substring(10)}"
        } else {
            raw.trim()
        }
    }
}
