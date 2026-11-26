package com.ucsm.dengueapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** true cuando la app está en modo oscuro. */
val LocalModoOscuro = staticCompositionLocalOf { false }

/** Función para cambiar el modo (true = oscuro, false = claro). La provee MainActivity. */
val LocalCambiarTema = staticCompositionLocalOf<(Boolean) -> Unit> { {} }

private val EsquemaClaro = lightColorScheme(
    primary = Azul700,
    onPrimary = Color.White,
    primaryContainer = AzulHielo,
    onPrimaryContainer = Color(0xFF0A2E6B),
    secondary = Azul500,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD6ECFD),
    onSecondaryContainer = Color(0xFF0A3558),
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
    surfaceVariant = Color(0xFFE4EDF8),
    onSurfaceVariant = Color(0xFF43546B),
    outline = Color(0xFF7C8DA6),
    outlineVariant = Color(0xFFD3E1F2)
)

private val EsquemaOscuro = darkColorScheme(
    primary = Color(0xFF64B5F6),
    onPrimary = Color(0xFF00264F),
    primaryContainer = Color(0xFF123A6B),
    onPrimaryContainer = Color(0xFFD6E9FF),
    secondary = Color(0xFF4FC3F7),
    onSecondary = Color(0xFF00263A),
    secondaryContainer = Color(0xFF0F3558),
    onSecondaryContainer = Color(0xFFD6ECFD),
    tertiary = Color(0xFFFFB74D),
    onTertiary = Color(0xFF3E2000),
    tertiaryContainer = Color(0xFF5A3A08),
    onTertiaryContainer = Color(0xFFFEF3C7),
    error = Color(0xFFFF8A80),
    onError = Color(0xFF410002),
    errorContainer = Color(0xFF5C1A1A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0A1424),
    onBackground = Color(0xFFE3ECF8),
    surface = Color(0xFF111E33),
    onSurface = Color(0xFFE3ECF8),
    surfaceVariant = Color(0xFF1C2D49),
    onSurfaceVariant = Color(0xFFB4C4DA),
    outline = Color(0xFF7F90AA),
    outlineVariant = Color(0xFF26395A)
)

private val Formas = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// Tipografía del sistema, con más contraste entre títulos y texto corrido
private val Tipografia = Typography(
    displaySmall = TextStyle(
        fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.3).sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.2).sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.2.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp, letterSpacing = 0.2.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 17.sp, letterSpacing = 0.3.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp)
)

@Composable
fun DengueAppTheme(
    oscuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalModoOscuro provides oscuro) {
        MaterialTheme(
            colorScheme = if (oscuro) EsquemaOscuro else EsquemaClaro,
            shapes = Formas,
            typography = Tipografia,
            content = content
        )
    }
}
