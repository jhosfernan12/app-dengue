package com.ucsm.dengueapp

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.ucsm.dengueapp.ui.theme.Azul500
import com.ucsm.dengueapp.ui.theme.Azul700
import com.ucsm.dengueapp.ui.theme.Cielo
import com.ucsm.dengueapp.ui.theme.LocalCambiarTema
import com.ucsm.dengueapp.ui.theme.LocalModoOscuro
import com.ucsm.dengueapp.ui.theme.NaranjaFebril
import com.ucsm.dengueapp.ui.theme.VerdeFuerte
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import com.ucsm.dengueapp.ui.theme.AlertaRoja
import com.ucsm.dengueapp.ui.theme.AmbarClaro
import com.ucsm.dengueapp.ui.theme.AmbarOscuro
import com.ucsm.dengueapp.ui.theme.VerdeClaro
import com.ucsm.dengueapp.ui.theme.VerdeOk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import androidx.compose.animation.core.animateFloat

// Teléfono de Infosalud (MINSA). VERIFICAR que siga vigente antes de publicar la app.
const val TELEFONO_INFOSALUD = "113"

fun textoDe(secciones: List<SeccionInfo>): String =
    secciones.joinToString(". ") { it.titulo + ". " + it.contenido.joinToString(". ") }

// ---------- Inicio ----------

/** Parte de arriba de la pantalla de inicio: degradado azul con la onda, nombre, mensaje y foto del mosquito. */
@Composable
fun HeroInicio() {
    val oscuro = LocalModoOscuro.current
    val cambiarTema = LocalCambiarTema.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(OndaInferior)
            .background(degradadoAzul(oscuro))
            .burbujas()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 64.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    NOMBRE_APP,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                IconButton(onClick = { cambiarTema(!oscuro) }) {
                    Icon(
                        if (oscuro) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        contentDescription = if (oscuro) "Cambiar a modo claro" else "Cambiar a modo oscuro",
                        tint = Color.White
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Aprende y previene el dengue",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Text(
                        "Información confiable, prevención en casa y herramientas para tu familia.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                }
                FotoCircular(R.drawable.img_mosquito_foto, 100.dp, "Fotografía de un mosquito Aedes")
            }
        }
    }
}

@Composable
private fun TarjetaAncha(
    titulo: String,
    detalle: String,
    icono: ImageVector,
    acento: Color,
    onClick: () -> Unit
) {
    val c = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = c.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, c.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(lerp(acento, Color.White, 0.28f), acento))),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(titulo, style = MaterialTheme.typography.titleSmall, color = c.onSurface)
                Text(detalle, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = c.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PantallaInicio(onIr: (Pantalla) -> Unit, escala: Float, onEscala: (Float) -> Unit) {
    val context = LocalContext.current
    val c = MaterialTheme.colorScheme
    val intro = "Aplicativo educativo. No diagnostica dengue ni reemplaza la evaluación de un profesional de salud. " +
            "Si tienes fiebre y alguna señal de alarma, acude de inmediato a un establecimiento de salud."

    Pagina(NOMBRE_APP, null, conBarraSuperior = false) {
        HeroInicio()

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ----- Qué hago ahora (lo más urgente, primero) -----
            Card(
                onClick = { onIr(Pantalla.Orientacion) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFFB71C1C), Color(0xFFE53935))))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.LocalHospital, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            "¿Tengo fiebre? ¿Qué hago ahora?",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Guía rápida: cuándo acudir al centro de salud",
                            color = Color.White.copy(alpha = 0.92f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.White)
                }
            }

            // ----- Escanear mosquito -----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(Azul500, Cielo)))
                    .burbujas()
                    .clickable { onIr(Pantalla.Mosquito) }
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FotoCircular(R.drawable.img_mosquito_foto, 72.dp, null)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Etiqueta("EXPERIMENTAL", Color.White.copy(alpha = 0.25f), Color.White)
                        Text(
                            "Identifica un mosquito",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            "Toma una foto y el modelo te dice si parece un Aedes.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.95f)
                        )
                    }
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }

            // ----- Explora -----
            TituloSeccion("Explora")

            Row(modifier = Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Baldosa("Información sobre el dengue", "Qué es y síntomas", Icons.Filled.Info, Azul700, Modifier.weight(1f)) { onIr(Pantalla.InfoGeneral) }
                Baldosa("Signos de alarma", "Cuándo acudir ya", Icons.Filled.Warning, AlertaRoja, Modifier.weight(1f)) { onIr(Pantalla.SignosAlarma) }
            }
            Row(modifier = Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Baldosa("Etapas de la enfermedad", "Fiebre, crítica y recuperación", Icons.Filled.Timeline, Color(0xFF5C6BC0), Modifier.weight(1f)) { onIr(Pantalla.Etapas) }
                Baldosa("Dengue vs. zancudo común", "Cómo diferenciarlos", Icons.Filled.BugReport, Color(0xFF0288D1), Modifier.weight(1f)) { onIr(Pantalla.Comparacion) }
            }
            Row(modifier = Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Baldosa("Prevención en el hogar", "Elimina los criaderos", Icons.Filled.Shield, VerdeFuerte, Modifier.weight(1f)) { onIr(Pantalla.Prevencion) }
                Baldosa("Revisa tu casa", "Lista de verificación", Icons.Filled.Checklist, Color(0xFF00897B), Modifier.weight(1f)) { onIr(Pantalla.Checklist) }
            }
            Row(modifier = Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Baldosa("Videos educativos", "Aprende mirando", Icons.Filled.PlayArrow, Azul500, Modifier.weight(1f)) { onIr(Pantalla.ListaVideos) }
                Baldosa("Mi rutina semanal", "Recordatorios", Icons.Filled.Notifications, Color(0xFF7E57C2), Modifier.weight(1f)) { onIr(Pantalla.Rutina) }
            }

            TarjetaAncha(
                "Mapa comunitario",
                "Mira y reporta criaderos cerca de ti",
                Icons.Filled.Place,
                NaranjaFebril
            ) { onIr(Pantalla.Mapa) }

            // ----- Galería (vista previa) -----
            TituloSeccion("Galería", "Ver todo") { onIr(Pantalla.Galeria) }
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ContenidoGaleria.fotos.take(6).forEach { img ->
                    Image(
                        painter = painterResource(img.recurso),
                        contentDescription = img.titulo,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(112.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onIr(Pantalla.Galeria) }
                    )
                }
            }

            // ----- Encuesta -----
            Card(
                onClick = { onIr(Pantalla.Encuesta) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = c.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(Icons.Filled.RateReview, contentDescription = null, tint = c.primary, modifier = Modifier.size(32.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Tu opinión nos ayuda a mejorar",
                            color = c.onPrimaryContainer,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Responde la encuesta de satisfacción (anónima, 2 minutos)",
                            color = c.onPrimaryContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            BotonEscuchar(intro)
            AyudaVoz()

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = c.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, c.outlineVariant)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Tamaño de letra", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("A" to 1f, "A+" to 1.3f, "A++" to 1.6f).forEach { (etiqueta, valor) ->
                            if (escala == valor) {
                                Button(onClick = { onEscala(valor) }, modifier = Modifier.weight(1f)) { Text(etiqueta) }
                            } else {
                                OutlinedButton(onClick = { onEscala(valor) }, modifier = Modifier.weight(1f)) { Text(etiqueta) }
                            }
                        }
                    }
                    HorizontalDivider()
                    Text("Tu actividad", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Etiqueta("${UsageLog.get(context, UsageLog.VIDEOS)} videos", c.primaryContainer, c.onPrimaryContainer)
                        Etiqueta("${UsageLog.get(context, UsageLog.REVISIONES)} revisiones", c.tertiaryContainer, c.onTertiaryContainer)
                        Etiqueta("${UsageLog.get(context, UsageLog.FOTOS)} fotos", c.secondaryContainer, c.onSecondaryContainer)
                    }
                }
            }

            Text(
                "Aplicativo educativo. No diagnostica dengue ni reemplaza la evaluación de un profesional de salud. " +
                        "Modelo de imágenes entrenado con MosqVision-3K (Masud, Md. A., 2024, Zenodo, licencia CC BY 4.0).",
                style = MaterialTheme.typography.bodySmall,
                color = c.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ---------- ¿Qué hago ahora? ----------

fun abrirCentrosDeSalud(context: Context) {
    val mapa = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode("centro de salud")))
    try {
        context.startActivity(mapa)
    } catch (e: ActivityNotFoundException) {
        val web = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/centro+de+salud"))
        try {
            context.startActivity(web)
        } catch (e2: ActivityNotFoundException) {
            Toast.makeText(context, "No se encontró una aplicación de mapas", Toast.LENGTH_LONG).show()
        }
    }
}

fun llamarInfosalud(context: Context) {
    try {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$TELEFONO_INFOSALUD")))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Este dispositivo no puede hacer llamadas", Toast.LENGTH_LONG).show()
    }
}

@Composable
fun ResultadoGuia(titulo: String, mensajes: List<String>, fondo: Color, texto: Color, icono: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = fondo)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(icono, contentDescription = null, tint = texto, modifier = Modifier.size(34.dp))
                Text(
                    titulo,
                    modifier = Modifier.weight(1f),
                    color = texto,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            mensajes.forEach { Vineta(it, texto) }
        }
    }
}

@Composable
fun PantallaOrientacion(onVolver: () -> Unit) {
    val context = LocalContext.current
    val c = MaterialTheme.colorScheme
    val signos = Contenido.signosAlarma.first().contenido
    var paso by remember { mutableIntStateOf(0) }          // 0 = fiebre, 1 = signos, 2 = resultado
    var hayFiebre by remember { mutableStateOf(false) }
    val marcados = remember { mutableStateListOf<Int>() }

    Pagina("¿Qué hago ahora?", onVolver) {
        AvisoCard("Esta guía es solo educativa. No diagnostica dengue ni reemplaza la evaluación de un profesional de salud.")

        when (paso) {
            0 -> {
                val pregunta = "¿Tienes o has tenido fiebre en los últimos días?"
                BotonEscuchar(pregunta)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = c.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Paso 1 de 2", style = MaterialTheme.typography.labelLarge, color = c.primary)
                        Text(pregunta, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { hayFiebre = true; paso = 1 },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                        ) { Text("Sí, tengo fiebre") }
                        OutlinedButton(
                            onClick = { hayFiebre = false; paso = 2 },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                        ) { Text("No tengo fiebre") }
                    }
                }
            }

            1 -> {
                val intro = "Marca si tienes alguno de estos signos de alarma."
                BotonEscuchar(intro + " " + signos.joinToString(". "))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = c.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Paso 2 de 2", style = MaterialTheme.typography.labelLarge, color = c.primary)
                        Text(intro, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        signos.forEachIndexed { i, signo ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (i in marcados) c.errorContainer else Color.Transparent)
                                    .clickable { if (i in marcados) marcados.remove(i) else marcados.add(i) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = i in marcados,
                                    onCheckedChange = { if (it) marcados.add(i) else marcados.remove(i) }
                                )
                                Text(signo, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                Button(
                    onClick = { paso = 2 },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                ) { Text(if (marcados.isEmpty()) "No tengo ninguno" else "Ver qué hacer") }
            }

            else -> {
                val titulo: String
                val mensajes: List<String>
                val fondo: Color
                val texto: Color
                val icono: ImageVector
                when {
                    !hayFiebre -> {
                        titulo = "Sin fiebre"
                        mensajes = listOf(
                            "Sin fiebre, esta guía no tiene indicaciones para ti.",
                            "Si te sientes mal por cualquier razón, consulta en un establecimiento de salud.",
                            "Sigue las medidas de prevención: revisa tu casa y elimina los criaderos del mosquito."
                        )
                        fondo = VerdeClaro
                        texto = VerdeOk
                        icono = Icons.Filled.Check
                    }
                    marcados.isNotEmpty() -> {
                        titulo = "Acude de inmediato a un establecimiento de salud"
                        mensajes = listOf(
                            "Tener fiebre y alguno de estos signos puede indicar dengue con signos de alarma o dengue grave.",
                            "No esperes a que empeore. Ve acompañado si es posible.",
                            "No te automediques. Evita aspirina e ibuprofeno."
                        )
                        fondo = AlertaRoja
                        texto = Color.White
                        icono = Icons.Filled.Warning
                    }
                    else -> {
                        titulo = "Consulta en un establecimiento de salud"
                        mensajes = listOf(
                            "Aunque no tengas signos de alarma, acude a un establecimiento de salud para que te evalúen, sobre todo si vives o viajaste a una zona con dengue.",
                            "No te automediques. Evita aspirina e ibuprofeno.",
                            "Toma bastante líquido y descansa, siguiendo las indicaciones del personal de salud.",
                            "Vigila los signos de alarma. Si aparece alguno, acude de inmediato."
                        )
                        fondo = AmbarClaro
                        texto = AmbarOscuro
                        icono = Icons.Filled.LocalHospital
                    }
                }

                BotonEscuchar(titulo + ". " + mensajes.joinToString(". "))
                ResultadoGuia(titulo, mensajes, fondo, texto, icono)

                if (hayFiebre) {
                    Button(
                        onClick = { abrirCentrosDeSalud(context) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    ) { Text("Buscar centro de salud cercano") }
                    OutlinedButton(
                        onClick = { llamarInfosalud(context) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    ) { Text("Llamar a Infosalud ($TELEFONO_INFOSALUD)") }
                }
                TextButton(
                    onClick = {
                        marcados.clear()
                        hayFiebre = false
                        paso = 0
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Volver a empezar") }
            }
        }
    }
}

// ---------- Pantallas informativas ----------

@Composable
fun PantallaInfoGeneral(onVolver: () -> Unit) {
    Pagina("Información sobre el dengue", onVolver) {
        FotoBanner(R.drawable.img_mosquito_foto, "Fotografía de un mosquito Aedes, el que transmite el dengue", pie = "Aedes aegypti, el mosquito que transmite el dengue")
        BotonEscuchar(textoDe(Contenido.informacionGeneral))
        SeccionInformativa(Contenido.informacionGeneral)
    }
}

@Composable
fun PantallaSignosAlarma(onVolver: () -> Unit) {
    Pagina("Signos de alarma", onVolver) {
        Ilustracion(R.drawable.img_fiebre, "Ilustración de un termómetro que marca fiebre alta")
        BotonEscuchar(textoDe(Contenido.signosAlarma))
        AvisoCard("Ante cualquier signo de alarma, acude de inmediato a un establecimiento de salud.")
        SeccionInformativa(Contenido.signosAlarma)
    }
}

@Composable
fun PantallaPrevencion(onVolver: () -> Unit) {
    Pagina("Prevención en el hogar", onVolver) {
        Ilustracion(R.drawable.img_criadero, "Ilustración de un balde con agua acumulada, un posible criadero")
        BotonEscuchar(textoDe(Contenido.prevencion))
        SeccionInformativa(Contenido.prevencion)
    }
}

// ---------- Videos ----------

@Composable
fun PantallaVideos(onAbrir: (Video) -> Unit, onVolver: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Pagina("Videos educativos", onVolver) {
        Cabecera(
            "Aprende con videos",
            "Videos oficiales de la OPS/OMS sobre síntomas, signos de alarma, cuidados y prevención."
        )
        Contenido.videos.groupBy { it.categoria }.forEach { (categoria, lista) ->
            TituloSeccion(categoria)
            lista.forEach { v ->
                Card(
                    onClick = { onAbrir(v) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = c.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = BorderStroke(1.dp, c.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.linearGradient(listOf(Azul500, Azul700))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(v.titulo, style = MaterialTheme.typography.titleSmall, color = c.onSurface)
                            Text("Fuente: ${v.fuente}", style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = c.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun YouTubeEmbebido(youtubeId: String, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webChromeClient = WebChromeClient()
                val html = """
                    <html><body style="margin:0;background:#000">
                    <iframe width="100%" height="100%" style="border:0"
                        src="https://www.youtube.com/embed/$youtubeId?playsinline=1&rel=0"
                        referrerpolicy="strict-origin-when-cross-origin"
                        allow="accelerometer; encrypted-media; gyroscope; picture-in-picture"
                        allowfullscreen></iframe>
                    </body></html>
                """.trimIndent()
                loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "utf-8", null)
            }
        },
        onRelease = { it.destroy() }
    )
}

@Composable
fun PantallaVideo(video: Video, onVolver: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(video.id) { UsageLog.registrarVideo(context, video.id) }
    Pagina(video.titulo, onVolver) {
        YouTubeEmbebido(
            video.youtubeId,
            Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(16.dp))
        )
        OutlinedButton(
            onClick = {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=${video.youtubeId}"))
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Si no carga, abrir en YouTube") }
        Text("Fuente: ${video.fuente}", style = MaterialTheme.typography.bodySmall)
    }
}

// ---------- Revisión del hogar ----------

@Composable
fun PantallaChecklist(onVolver: () -> Unit) {
    val context = LocalContext.current
    val c = MaterialTheme.colorScheme
    val marcados = remember { mutableStateListOf<Int>() }
    val total = Contenido.checklist.size

    Pagina("Revisa tu casa", onVolver) {
        Text(
            "Marca lo que ya revisaste. Los recipientes con agua acumulada pueden ser posibles criaderos del mosquito.",
            style = MaterialTheme.typography.bodyMedium
        )
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = c.primaryContainer)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "${marcados.size} de $total revisados",
                    fontWeight = FontWeight.Bold,
                    color = c.onPrimaryContainer
                )
                LinearProgressIndicator(
                    progress = { if (total == 0) 0f else marcados.size.toFloat() / total },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = c.primary,
                    trackColor = c.surface
                )
            }
        }
        Contenido.checklist.forEachIndexed { i, (lugar, accion) ->
            val hecho = i in marcados
            Card(
                onClick = { if (hecho) marcados.remove(i) else marcados.add(i) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (hecho) VerdeClaro else c.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = hecho,
                        onCheckedChange = { if (it) marcados.add(i) else marcados.remove(i) }
                    )
                    Column(Modifier.weight(1f)) {
                        Text(lugar, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(accion, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
                    }
                }
            }
        }
        Button(
            onClick = {
                UsageLog.increment(context, UsageLog.REVISIONES)
                Rutina.registrarRevision(context)
                onVolver()
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
        ) { Text("Guardar revisión") }
    }
}

// ---------- Identificar mosquito (IA) ----------

const val UMBRAL = 0.70f

fun nuevaUriFoto(context: Context): Uri {
    val carpeta = File(context.cacheDir, "fotos").apply { mkdirs() }
    val archivo = File(carpeta, "mosquito_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
}

fun cargarBitmap(context: Context, uri: Uri): Bitmap {
    val fuente = ImageDecoder.createSource(context.contentResolver, uri)
    return ImageDecoder.decodeBitmap(fuente) { decoder, info, _ ->
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        val lado = maxOf(info.size.width, info.size.height)
        if (lado > 1024) {
            val escala = 1024f / lado
            decoder.setTargetSize((info.size.width * escala).toInt(), (info.size.height * escala).toInt())
        }
    }
}

/** Alerta roja: el modelo cree que es Aedes y con suficiente confianza. */
fun esAlertaAedes(p: Prediccion): Boolean = p.etiqueta == "Aedes" && p.confianza >= UMBRAL

fun nombreAmigable(etiqueta: String): String = when (etiqueta) {
    "Aedes" -> "Aedes"
    "OtroMosquito" -> "Otro mosquito"
    "Otro" -> "No es un mosquito"
    else -> etiqueta
}

@Composable
fun BarrasConfianza(r: Prediccion, colorTexto: Color, colorBarra: Color, colorPista: Color) {
    r.todas.forEach { (nombre, p) ->
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "${nombreAmigable(nombre)}: ${(p * 100).toInt()}%",
                color = colorTexto,
                style = MaterialTheme.typography.bodySmall
            )
            LinearProgressIndicator(
                progress = { p.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = colorBarra,
                trackColor = colorPista
            )
        }
    }
}

@Composable
fun TarjetaAlerta(r: Prediccion, onCriaderos: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(Unit) { haptic.performHapticFeedback(HapticFeedbackType.LongPress) }

    val transicion = rememberInfiniteTransition(label = "alerta")
    val brillo by transicion.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "brillo"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AlertaRoja),
        border = BorderStroke(4.dp, Color(0xFFFF8A80).copy(alpha = brillo))
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(38.dp))
                Text(
                    "ALERTA: POSIBLE AEDES",
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Text(
                "El modelo detectó un mosquito del género Aedes, que incluye al que transmite el dengue.",
                color = Color.White
            )
            Text(
                "Esto NO confirma que sea Aedes aegypti ni que haya dengue, pero es una señal para actuar.",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text("Qué hacer ahora:", color = Color.White, fontWeight = FontWeight.Bold)
            Vineta("Revisa y elimina posibles criaderos en tu casa.", Color.White)
            Vineta("Protégete de las picaduras con mosquitero o repelente.", Color.White)
            Vineta("Si tú o alguien de tu casa tiene fiebre, acude a un establecimiento de salud.", Color.White)
            Text(
                "Confianza del modelo: ${(r.confianza * 100).toInt()}%",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge
            )
            BarrasConfianza(r, Color.White, Color.White, Color.White.copy(alpha = 0.25f))
            Button(
                onClick = onCriaderos,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = AlertaRoja)
            ) { Text("Revisar mi casa ahora", fontWeight = FontWeight.Bold) }
            Text(
                "Resultado orientativo de una función experimental. No es una identificación entomológica ni una notificación oficial.",
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun TarjetaNormal(r: Prediccion) {
    val c = MaterialTheme.colorScheme
    val otroMosquito = r.etiqueta == "OtroMosquito" && r.confianza >= UMBRAL
    val fondo = if (otroMosquito) AmbarClaro else c.surfaceVariant
    val texto = if (otroMosquito) AmbarOscuro else c.onSurfaceVariant
    val titulo = if (otroMosquito) "Posible mosquito de otro género" else "Imagen no concluyente"
    val mensaje = if (otroMosquito) {
        "El modelo lo reconoce como un mosquito que no parece Aedes, pero esto no es una confirmación. Mantén las medidas de prevención."
    } else {
        "No se pudo reconocer un mosquito con suficiente seguridad. Intenta otra foto: más cerca, con buena luz y fondo claro."
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = fondo)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(titulo, color = texto, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(mensaje, color = texto)
            Text(
                "Confianza del modelo: ${(r.confianza * 100).toInt()}%",
                color = texto,
                style = MaterialTheme.typography.labelLarge
            )
            BarrasConfianza(r, texto, c.primary, c.surface)
            Text(
                "Resultado orientativo de una función experimental. No es una identificación entomológica ni una notificación oficial.",
                color = texto,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun TarjetaResultado(r: Prediccion, onCriaderos: () -> Unit) {
    if (esAlertaAedes(r)) TarjetaAlerta(r, onCriaderos) else TarjetaNormal(r)
}

@Composable
fun PantallaMosquito(onVolver: () -> Unit, onChecklist: () -> Unit) {
    val context = LocalContext.current
    // Si el modelo falla al cargar, se muestra el motivo en pantalla en vez de cerrar la app
    val carga = remember { runCatching { MosquitoClassifier(context) } }
    val clasificador = carga.getOrNull()
    DisposableEffect(Unit) { onDispose { clasificador?.close() } }

    var fotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var pendiente by rememberSaveable { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var resultado by remember { mutableStateOf<Prediccion?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val tomarFoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) fotoUri = pendiente
    }
    val elegirFoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) fotoUri = uri
    }

    LaunchedEffect(fotoUri) {
        val uri = fotoUri ?: return@LaunchedEffect
        val modelo = clasificador ?: return@LaunchedEffect
        try {
            val (bmp, pred) = withContext(Dispatchers.Default) {
                val b = cargarBitmap(context, uri)
                b to modelo.classify(b)
            }
            bitmap = bmp
            resultado = pred
            error = null
            UsageLog.increment(context, UsageLog.FOTOS)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = "No se pudo analizar la imagen: ${e.message}"
        }
    }

    val alerta = resultado?.let { esAlertaAedes(it) } == true

    Pagina("Identificar mosquito", onVolver) {
        AvisoCard("FUNCIÓN EXPERIMENTAL: el resultado es solo orientativo y no confirma la presencia de Aedes aegypti.")
        carga.exceptionOrNull()?.let {
            AvisoCard("No se pudo cargar el modelo: ${it.javaClass.simpleName}: ${it.message}")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    val uri = nuevaUriFoto(context)
                    pendiente = uri
                    tomarFoto.launch(uri)
                },
                enabled = clasificador != null,
                modifier = Modifier.weight(1f).heightIn(min = 56.dp)
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Tomar foto")
            }
            OutlinedButton(
                onClick = {
                    elegirFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                enabled = clasificador != null,
                modifier = Modifier.weight(1f).heightIn(min = 56.dp)
            ) { Text("Galería") }
        }
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Foto analizada",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .then(
                        if (alerta) Modifier.border(4.dp, AlertaRoja, RoundedCornerShape(16.dp))
                        else Modifier
                    )
            )
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        resultado?.let { TarjetaResultado(it, onChecklist) }
    }
}