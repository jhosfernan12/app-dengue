package com.ucsm.dengueapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.ucsm.dengueapp.ui.theme.DengueAppTheme
import com.ucsm.dengueapp.ui.theme.LocalCambiarTema

sealed interface Pantalla {
    data object Inicio : Pantalla
    data object Orientacion : Pantalla
    data object InfoGeneral : Pantalla
    data object SignosAlarma : Pantalla
    data object Etapas : Pantalla
    data object Comparacion : Pantalla
    data object Prevencion : Pantalla
    data object ListaVideos : Pantalla
    data class VerVideo(val video: Video) : Pantalla
    data object Checklist : Pantalla
    data object Rutina : Pantalla
    data object Mapa : Pantalla
    data object Mosquito : Pantalla
    data object Encuesta : Pantalla
    data object Galeria : Pantalla
    data object Sintomas : Pantalla
    data object Quiz : Pantalla
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            val sistemaOscuro = isSystemInDarkTheme()
            val prefs = remember { getSharedPreferences("ajustes", Context.MODE_PRIVATE) }
            // 0 = seguir al teléfono, 1 = claro, 2 = oscuro
            var modo by remember { mutableIntStateOf(prefs.getInt("tema", 0)) }
            val oscuro = when (modo) {
                1 -> false
                2 -> true
                else -> sistemaOscuro
            }

            DisposableEffect(oscuro) {
                this@MainActivity.enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
                    navigationBarStyle = if (oscuro) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    }
                )
                onDispose { }
            }

            val cambiarTema: (Boolean) -> Unit = { quiereOscuro ->
                modo = if (quiereOscuro) 2 else 1
                prefs.edit().putInt("tema", modo).apply()
            }

            CompositionLocalProvider(LocalCambiarTema provides cambiarTema) {
                DengueAppTheme(oscuro = oscuro) {
                    AppDengue()
                }
            }
        }
    }
}

@Composable
fun AppDengue() {
    val context = LocalContext.current
    var pantalla by remember { mutableStateOf<Pantalla>(Pantalla.Inicio) }
    // La carátula se muestra al abrir la app (y no vuelve a aparecer al girar el teléfono)
    var mostrarCaratula by rememberSaveable { mutableStateOf(true) }

    var escala by remember {
        mutableFloatStateOf(
            context.getSharedPreferences("ajustes", Context.MODE_PRIVATE).getFloat("escala", 1f)
        )
    }
    val cambiarEscala: (Float) -> Unit = { nueva ->
        escala = nueva
        context.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
            .edit().putFloat("escala", nueva).apply()
    }
    val densidad = LocalDensity.current

    val atras: () -> Unit = {
        pantalla = when (pantalla) {
            is Pantalla.VerVideo -> Pantalla.ListaVideos
            else -> Pantalla.Inicio
        }
    }

    BackHandler(enabled = !mostrarCaratula && pantalla != Pantalla.Inicio) {
        atras()
    }

    CompositionLocalProvider(
        LocalDensity provides Density(densidad.density, fontScale = densidad.fontScale * escala),
        LocalNavegacion provides Navegacion(actual = pantalla, ir = { pantalla = it })
    ) {
        if (mostrarCaratula) {
            PantallaCarga(onTerminar = { mostrarCaratula = false })
        } else {
            when (val p = pantalla) {
                Pantalla.Inicio -> PantallaInicio(
                    onIr = { pantalla = it },
                    escala = escala,
                    onEscala = cambiarEscala
                )

                Pantalla.Orientacion -> PantallaOrientacion(onVolver = atras)

                Pantalla.InfoGeneral -> PantallaInfoGeneral(onVolver = atras)

                Pantalla.SignosAlarma -> PantallaSignosAlarma(onVolver = atras)

                Pantalla.Etapas -> PantallaEtapas(onVolver = atras)

                Pantalla.Comparacion -> PantallaComparacion(onVolver = atras)

                Pantalla.Prevencion -> PantallaPrevencion(onVolver = atras)

                Pantalla.ListaVideos -> PantallaVideos(
                    onAbrir = { pantalla = Pantalla.VerVideo(it) },
                    onVolver = atras
                )

                is Pantalla.VerVideo -> PantallaVideo(
                    video = p.video,
                    onVolver = atras
                )

                Pantalla.Checklist -> PantallaChecklist(onVolver = atras)

                Pantalla.Rutina -> PantallaRutina(
                    onVolver = atras,
                    onRevisar = { pantalla = Pantalla.Checklist }
                )

                Pantalla.Mapa -> PantallaMapa(
                    onVolver = atras,
                    onChecklist = { pantalla = Pantalla.Checklist }
                )

                Pantalla.Mosquito -> PantallaMosquito(
                    onVolver = atras,
                    onChecklist = { pantalla = Pantalla.Checklist }
                )

                Pantalla.Encuesta -> PantallaEncuesta(onVolver = atras)

                Pantalla.Galeria -> PantallaGaleria(onVolver = atras)

                Pantalla.Sintomas -> PantallaSintomas(onVolver = atras, onIr = { pantalla = it })

                Pantalla.Quiz -> PantallaQuiz(onVolver = atras, onIr = { pantalla = it })
            }
        }
    }
}