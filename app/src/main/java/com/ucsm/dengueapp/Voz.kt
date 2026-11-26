package com.ucsm.dengueapp

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

// ---------- Lectura en voz alta (accesibilidad) ----------
//
// La app no trae voces propias: usa el motor de texto a voz del teléfono. Para que suene menos
// robótica hace tres cosas: (1) elige la mejor voz en español instalada, (2) lee frase por frase
// con pequeñas pausas y un poco más despacio, y (3) corrige la pronunciación de palabras como
// "Aedes aegypti". La calidad final depende de las voces que tenga instaladas el teléfono.

private val PRONUNCIACIONES = listOf(
    "Aedes aegypti" to "Aédes egipti",
    "aegypti" to "egipti",
    "Aedes" to "Aédes",
    "Culex" to "Cúlex",
    "Anopheles" to "Anófeles",
    "DEN-1" to "dengue uno",
    "DEN-2" to "dengue dos",
    "DEN-3" to "dengue tres",
    "DEN-4" to "dengue cuatro"
)

/** Limpia el texto para que el motor de voz lo lea de forma natural. */
fun prepararTexto(original: String): String {
    var t = original
    PRONUNCIACIONES.forEach { (escrito, leido) -> t = t.replace(escrito, leido) }
    t = t.replace("(", ", ").replace(")", ", ").replace("—", ", ").replace("•", " ")
    t = t.replace(Regex("\\s+"), " ")
    t = t.replace(Regex("\\.\\s*\\."), ".")
    t = t.replace(Regex(",\\s*,"), ",")
    t = t.replace(Regex(",\\s*\\."), ".")
    return t.trim()
}

/** Parte el texto en frases; las frases muy largas se cortan en las comas. */
fun dividirEnFrases(texto: String): List<String> {
    val frases = texto.split(Regex("(?<=[.!?;:])\\s+")).map { it.trim() }.filter { it.isNotEmpty() }
    val resultado = mutableListOf<String>()
    for (frase in frases) {
        if (frase.length <= 320) {
            resultado.add(frase)
        } else {
            var actual = ""
            for (trozo in frase.split(Regex("(?<=,)\\s+"))) {
                if (actual.isNotEmpty() && actual.length + trozo.length > 260) {
                    resultado.add(actual.trim())
                    actual = ""
                }
                actual += "$trozo "
            }
            if (actual.isNotBlank()) resultado.add(actual.trim())
        }
    }
    return resultado
}

private fun puntajeRegion(locale: Locale): Int = when (locale.country) {
    "PE" -> 5
    "US", "419" -> 4
    "MX" -> 3
    "ES" -> 1
    else -> 2
}

class Lector(context: Context) {
    private val appContext = context.applicationContext
    private val principal = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = null
    private var listo = false
    private var sesion = 0
    private var ultimoId: String? = null
    private var alTerminar: (() -> Unit)? = null

    init {
        tts = TextToSpeech(appContext) { estado ->
            if (estado == TextToSpeech.SUCCESS) {
                val motor = tts
                var r = motor?.setLanguage(Locale("es", "PE")) ?: TextToSpeech.LANG_NOT_SUPPORTED
                if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
                    r = motor?.setLanguage(Locale("es")) ?: TextToSpeech.LANG_NOT_SUPPORTED
                }
                listo = r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED
                motor?.setSpeechRate(0.92f)
                motor?.setPitch(1.0f)
                motor?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}

                    override fun onDone(utteranceId: String?) {
                        terminado(utteranceId)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        terminado(utteranceId)
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        terminado(utteranceId)
                    }

                    override fun onStop(utteranceId: String?, interrupted: Boolean) {
                        terminado(utteranceId)
                    }
                })
            }
        }
    }

    private fun terminado(id: String?) {
        if (id != null && id == ultimoId) {
            val aviso = alTerminar
            principal.post { aviso?.invoke() }
        }
    }

    /** Elige la mejor voz en español: primero la de más calidad, luego la región más cercana. */
    private fun elegirVoz(motor: TextToSpeech) {
        val enLinea = hayInternet(appContext)
        val todas: Set<Voice> = runCatching { motor.voices.orEmpty() }.getOrDefault(emptySet())
        val mejor = todas
            .filter { it.locale.language == "es" }
            .filter { !it.features.orEmpty().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) }
            .filter { enLinea || !it.isNetworkConnectionRequired }
            .maxWithOrNull(
                compareBy<Voice> { it.quality }
                    .thenBy { puntajeRegion(it.locale) }
                    .thenBy { if (it.isNetworkConnectionRequired) 0 else 1 }
            )
        if (mejor != null) {
            runCatching { motor.setVoice(mejor) }
        }
    }

    fun hablar(texto: String, alTerminar: () -> Unit = {}): Boolean {
        val motor = tts ?: return false
        if (!listo) return false
        val frases = dividirEnFrases(prepararTexto(texto))
        if (frases.isEmpty()) return false

        elegirVoz(motor)
        sesion += 1
        val id = sesion
        this.alTerminar = alTerminar
        ultimoId = "d$id-${frases.size - 1}"

        motor.stop()   // reemplaza cualquier lectura anterior
        frases.forEachIndexed { i, frase ->
            motor.speak(frase, TextToSpeech.QUEUE_ADD, null, "d$id-$i")
            if (i < frases.size - 1) {
                val pausa = if (frase.endsWith(":")) 450L else 260L
                motor.playSilentUtterance(pausa, TextToSpeech.QUEUE_ADD, "d$id-$i-pausa")
            }
        }
        return true
    }

    fun parar() {
        alTerminar = null
        tts?.stop()
    }

    fun cerrar() {
        alTerminar = null
        tts?.stop()
        tts?.shutdown()
    }
}

@Composable
fun BotonEscuchar(texto: String) {
    val context = LocalContext.current
    val lector = remember { Lector(context) }
    var hablando by remember(texto) { mutableStateOf(false) }

    DisposableEffect(texto) { onDispose { lector.parar() } }
    DisposableEffect(Unit) { onDispose { lector.cerrar() } }

    FilledTonalButton(
        onClick = {
            if (hablando) {
                lector.parar()
                hablando = false
            } else if (lector.hablar(texto) { hablando = false }) {
                hablando = true
            } else {
                Toast.makeText(
                    context,
                    "La lectura en voz alta no está disponible en este teléfono",
                    Toast.LENGTH_LONG
                ).show()
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(if (hablando) "⏹  Detener lectura" else "🔊  Escuchar esta pantalla")
    }
}

/** Abre los ajustes de "texto a voz" del teléfono, donde se puede instalar una voz mejor. */
fun abrirAjustesVoz(context: Context) {
    val intentos = listOf(
        Intent("com.android.settings.TTS_SETTINGS"),
        Intent(Settings.ACTION_SETTINGS)
    )
    for (intento in intentos) {
        try {
            context.startActivity(intento)
            return
        } catch (e: ActivityNotFoundException) {
            // probar con el siguiente
        } catch (e: SecurityException) {
            // probar con el siguiente
        }
    }
    Toast.makeText(context, "No se pudieron abrir los ajustes del teléfono", Toast.LENGTH_LONG).show()
}

@Composable
fun AyudaVoz() {
    val context = LocalContext.current
    TextButton(
        onClick = { abrirAjustesVoz(context) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("¿La voz suena robótica? Mejórala en los ajustes del teléfono")
    }
}
