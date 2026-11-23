package com.ucsm.dengueapp.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// ---------- Colores fijos (iguales en claro y oscuro) ----------

val Teal700 = Color(0xFF0F766E)
val TealClaro = Color(0xFFCCFBF1)
val TealOscuro = Color(0xFF134E4A)

val Azul700 = Color(0xFF0369A1)
val AzulProfundo = Color(0xFF0C4A6E)

val Ambar600 = Color(0xFFD97706)

val AlertaRoja = Color(0xFFB71C1C)
val NaranjaFebril = Color(0xFFEF6C00)
val VerdeFuerte = Color(0xFF2E7D32)

// Versiones más brillantes, para texto sobre fondo oscuro
val NaranjaAcento = Color(0xFFFFA726)
val RojoAcento = Color(0xFFEF5350)
val VerdeAcento = Color(0xFF66BB6A)

val FondoApp = Color(0xFFF4F9F8)
val TextoOscuro = Color(0xFF0F1F1D)

// ---------- Colores que cambian según el modo ----------

val VerdeClaro: Color
    @Composable @ReadOnlyComposable
    get() = if (LocalModoOscuro.current) Color(0xFF1B3A1F) else Color(0xFFE8F5E9)

val VerdeOk: Color
    @Composable @ReadOnlyComposable
    get() = if (LocalModoOscuro.current) Color(0xFF81C784) else Color(0xFF2E7D32)

val AmbarClaro: Color
    @Composable @ReadOnlyComposable
    get() = if (LocalModoOscuro.current) Color(0xFF3E2C0B) else Color(0xFFFEF3C7)

val AmbarOscuro: Color
    @Composable @ReadOnlyComposable
    get() = if (LocalModoOscuro.current) Color(0xFFFFD180) else Color(0xFF78350F)

val RojoClaro: Color
    @Composable @ReadOnlyComposable
    get() = if (LocalModoOscuro.current) Color(0xFF3F1515) else Color(0xFFFEE2E2)

val RojoOscuro: Color
    @Composable @ReadOnlyComposable
    get() = if (LocalModoOscuro.current) Color(0xFFFFCDD2) else Color(0xFF7F1D1D)

val AzulClaro: Color
    @Composable @ReadOnlyComposable
    get() = if (LocalModoOscuro.current) Color(0xFF0F2F44) else Color(0xFFE0F2FE)

val AzulOscuro: Color
    @Composable @ReadOnlyComposable
    get() = if (LocalModoOscuro.current) Color(0xFFB3E5FC) else Color(0xFF0C4A6E)