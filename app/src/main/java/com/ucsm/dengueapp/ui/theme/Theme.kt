package com.ucsm.dengueapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** true cuando la app está en modo oscuro. */
val LocalModoOscuro = staticCompositionLocalOf { false }

/** Función para cambiar el modo (true = oscuro, false = claro). La provee MainActivity. */
val LocalCambiarTema = staticCompositionLocalOf<(Boolean) -> Unit> { {} }

private val EsquemaClaro = lightColorScheme(
    primary = Teal700,
    onPrimary = Color.White,
    primaryContainer = TealClaro,
    onPrimaryContainer = TealOscuro,
    secondary = Azul700,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0C4A6E),
    tertiary = Ambar600,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFF78350F),
    error = AlertaRoja,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
    background = FondoApp,
    onBackground = TextoOscuro,
    surface = Color.White,
    onSurface = TextoOscuro,
    surfaceVariant = Color(0xFFE2ECEA),
    onSurfaceVariant = Color(0xFF40524F),
    outline = Color(0xFF7A8D8A)
)

private val EsquemaOscuro = darkColorScheme(
    primary = Color(0xFF4DB6AC),
    onPrimary = Color(0xFF00201D),
    primaryContainer = Color(0xFF134E4A),
    onPrimaryContainer = Color(0xFFCCFBF1),
    secondary = Color(0xFF7CC4EC),
    onSecondary = Color(0xFF00263A),
    secondaryContainer = Color(0xFF0C4A6E),
    onSecondaryContainer = Color(0xFFE0F2FE),
    tertiary = Color(0xFFFFB74D),
    onTertiary = Color(0xFF3E2000),
    tertiaryContainer = Color(0xFF5A3A08),
    onTertiaryContainer = Color(0xFFFEF3C7),
    error = Color(0xFFFF8A80),
    onError = Color(0xFF410002),
    errorContainer = Color(0xFF5C1A1A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0E1514),
    onBackground = Color(0xFFE2ECEA),
    surface = Color(0xFF161E1D),
    onSurface = Color(0xFFE2ECEA),
    surfaceVariant = Color(0xFF2A3735),
    onSurfaceVariant = Color(0xFFB5C7C4),
    outline = Color(0xFF869996)
)

@Composable
fun DengueAppTheme(
    oscuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalModoOscuro provides oscuro) {
        MaterialTheme(
            colorScheme = if (oscuro) EsquemaOscuro else EsquemaClaro,
            content = content
        )
    }
}