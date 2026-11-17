package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val RoccaLightColorScheme = lightColorScheme(
    primary = RoccaRojo,
    onPrimary = RoccaBlanco,
    primaryContainer = RoccaRojoClaro,
    onPrimaryContainer = RoccaRojoOscuro,
    secondary = RoccaAzulOscuro,
    onSecondary = RoccaBlanco,
    secondaryContainer = RoccaTarjeta,
    onSecondaryContainer = RoccaTexto,
    tertiary = RoccaAviso,
    onTertiary = RoccaAzulOscuro,
    tertiaryContainer = RoccaAvisoFondo,
    onTertiaryContainer = RoccaAvisoTextoOscuro,
    background = RoccaFondo,
    onBackground = RoccaTexto,
    surface = RoccaBlanco,
    onSurface = RoccaTexto,
    surfaceVariant = RoccaTarjeta,
    onSurfaceVariant = RoccaTextoSuave,
    outline = RoccaBordeInput,
    outlineVariant = RoccaBorde,
    error = RoccaError,
    onError = RoccaBlanco,
    errorContainer = RoccaErrorFondo,
    onErrorContainer = RoccaErrorTexto,
)

val RoccaShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
)

@Composable
fun RoccaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RoccaLightColorScheme,
        typography = RoccaTypography,
        shapes = RoccaShapes,
        content = content,
    )
}
