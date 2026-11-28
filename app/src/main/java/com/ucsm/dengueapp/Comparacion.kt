package com.ucsm.dengueapp

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ucsm.dengueapp.ui.theme.AlertaRoja
import com.ucsm.dengueapp.ui.theme.Azul700
import com.ucsm.dengueapp.ui.theme.AzulClaro
import com.ucsm.dengueapp.ui.theme.AzulOscuro
import com.ucsm.dengueapp.ui.theme.RojoClaro
import com.ucsm.dengueapp.ui.theme.RojoOscuro

/*
 * Pantalla "Dengue vs. zancudo común" con fotos en cada punto de la comparación.
 *
 * - Las fotos del mosquito del dengue y de los criaderos vienen dentro de la app.
 * - Las fotos del zancudo común (Culex) son del CDC (dominio público). Se cargan así:
 *     1) si existe el archivo en res/drawable-nodpi/ (zancudo_01_reposo.jpg, etc.), se usa ese;
 *     2) si no, se descargan de internet al abrir la pantalla;
 *     3) si no hay internet, se muestra un recuadro con ícono.
 */

private const val CDC = "https://wwwn.cdc.gov/phil/PHIL_Images/"
private const val URL_CULEX_REPOSO = CDC + "26026/26026_lores.jpg"
private const val URL_CULEX_FLOR = CDC + "26027/26027_lores.jpg"
private const val URL_CULEX_PICANDO = CDC + "26025/26025_lores.jpg"
private const val URL_CULEX_ALAS = CDC + "26028/26028_lores.jpg"

private val PanelGris = Color(0xFFE8EEF5)
private val IconoGris = Color(0xFF5B6B7F)

/** Una foto de la comparación: de la app (local), de internet (url) o solo un recuadro con ícono. */
private class FotoDef(
    @DrawableRes val local: Int,
    val url: String?,
    val pie: String,
    val icono: ImageVector,
    val relacion: Float = 1.2f,
    val colorPanel: Color = PanelGris,
    val colorIcono: Color = IconoGris
)

/** Busca un drawable por nombre. Devuelve 0 si no existe. */
@Composable
private fun idOpcional(vararg nombres: String): Int {
    val context = LocalContext.current
    return remember {
        nombres.firstNotNullOfOrNull { n ->
            context.resources.getIdentifier(n, "drawable", context.packageName).takeIf { it != 0 }
        } ?: 0
    }
}

/**
 * Foto con una franja de texto abajo. Debajo de la foto siempre hay un recuadro con ícono:
 * se ve mientras carga o si no hay internet.
 */
@Composable
private fun FotoConPie(foto: FotoDef, modifier: Modifier = Modifier) {
    var cargada by remember(foto.url) { mutableStateOf(false) }
    val hayFoto = foto.local != 0 || cargada

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(foto.relacion)
            .clip(RoundedCornerShape(14.dp))
            .background(foto.colorPanel)
    ) {
        if (!hayFoto) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(foto.icono, contentDescription = null, tint = foto.colorIcono)
                Text(
                    foto.pie,
                    color = foto.colorIcono,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (foto.local != 0) {
            Image(
                painter = painterResource(foto.local),
                contentDescription = foto.pie,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (foto.url != null) {
            AsyncImage(
                model = foto.url,
                contentDescription = foto.pie,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onSuccess = { cargada = true }
            )
        }

        if (hayFoto) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC000000))))
                    .padding(start = 8.dp, end = 8.dp, bottom = 6.dp, top = 22.dp)
            ) {
                Text(
                    foto.pie,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Una fila de la comparación. Las fotos van en pares (una de cada mosquito, una al lado de la otra)
 * para que siempre queden alineadas; abajo va el texto de cada uno.
 */
@Composable
private fun FilaVisual(
    fila: FilaComparacion,
    icono: ImageVector,
    pares: List<Pair<FotoDef, FotoDef>>
) {
    val c = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = c.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icono, contentDescription = null, tint = c.primary)
                Text(
                    fila.aspecto,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = c.primary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) { Etiqueta("Mosquito del dengue", AlertaRoja, Color.White) }
                Box(Modifier.weight(1f)) { Etiqueta("Zancudo común", Azul700, Color.White) }
            }

            pares.forEach { (dengue, comun) ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FotoConPie(dengue, Modifier.weight(1f))
                    FotoConPie(comun, Modifier.weight(1f))
                }
            }

            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .background(RojoClaro)
                        .padding(10.dp)
                ) {
                    Text(fila.dengue, color = RojoOscuro, style = MaterialTheme.typography.bodySmall)
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AzulClaro)
                        .padding(10.dp)
                ) {
                    Text(fila.comun, color = AzulOscuro, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun RetratoMosquito(
    foto: FotoDef,
    titulo: String,
    nombre: String,
    acento: Color,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = c.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            FotoConPie(foto)
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = acento
                )
                Text(nombre, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
            }
        }
    }
}

// ---------- Pantalla ----------

@Composable
fun PantallaComparacion(onVolver: () -> Unit) {
    val c = MaterialTheme.colorScheme
    val filas = Contenido.comparacion

    val textoVoz = "Mosquito del dengue frente a zancudo común. " +
            filas.joinToString(". ") {
                "${it.aspecto}. Mosquito del dengue: ${it.dengue} Zancudo común: ${it.comun}"
            } + ". No se puede asegurar la especie solo mirando. Ante la duda, elimina criaderos y consulta a un establecimiento de salud."

    // Fotos del zancudo común (Culex): archivo local si existe, si no, internet
    val reposo = FotoDef(
        idOpcional("zancudo_01_reposo", "zancudo_comun"), URL_CULEX_REPOSO,
        "Zancudo común (Culex)", Icons.Filled.BugReport, relacion = 1f
    )
    val flor = FotoDef(
        idOpcional("zancudo_02_flor"), URL_CULEX_FLOR,
        "Cuerpo pardo, sin manchas", Icons.Filled.BugReport
    )
    val alas = FotoDef(
        idOpcional("zancudo_04_alas"), URL_CULEX_ALAS,
        "Alas y abdomen sin rayas blancas", Icons.Filled.BugReport
    )
    val picando = FotoDef(
        idOpcional("zancudo_03_picando"), URL_CULEX_PICANDO,
        "Pica de noche", Icons.Filled.NightsStay
    )

    Pagina("Dengue vs. zancudo común", onVolver) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RetratoMosquito(
                FotoDef(R.drawable.img_mosquito_foto, null, "Mosquito del dengue", Icons.Filled.BugReport, relacion = 1f),
                "Mosquito del dengue", "Aedes aegypti", AlertaRoja, Modifier.weight(1f)
            )
            RetratoMosquito(reposo, "Zancudo común", "Culex", Azul700, Modifier.weight(1f))
        }

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = c.secondaryContainer)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "¿Cómo diferenciarlos?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = c.onSecondaryContainer
                )
                Text(
                    "El mosquito que transmite el dengue (Aedes aegypti) y el zancudo común (Culex) se parecen, " +
                            "pero tienen diferencias en su aspecto, en cuándo pican y en dónde se crían. " +
                            "Mira las fotos de cada punto.",
                    color = c.onSecondaryContainer
                )
            }
        }

        BotonEscuchar(textoVoz)

        // 1. Cómo se ve
        filas.getOrNull(0)?.let { fila ->
            FilaVisual(
                fila, Icons.Filled.Visibility,
                listOf(
                    FotoDef(R.drawable.gal_01_perfil, null, "Cuerpo oscuro con manchas blancas", Icons.Filled.BugReport) to flor,
                    FotoDef(R.drawable.gal_06_patas, null, "Bandas blancas en las patas", Icons.Filled.BugReport) to alas
                )
            )
        }

        // 2. Cuándo pica
        filas.getOrNull(1)?.let { fila ->
            FilaVisual(
                fila, Icons.Filled.Schedule,
                listOf(
                    FotoDef(R.drawable.gal_07_aedes_piel_a, null, "Pica de día", Icons.Filled.WbSunny) to picando
                )
            )
        }

        // 3. Cómo suena
        filas.getOrNull(2)?.let { fila ->
            FilaVisual(
                fila, Icons.Filled.VolumeUp,
                listOf(
                    FotoDef(0, null, "Casi no se oye", Icons.Filled.VolumeOff, relacion = 2.2f) to
                            FotoDef(0, null, "Zumbido cerca del oído", Icons.Filled.VolumeUp, relacion = 2.2f)
                )
            )
        }

        // 4. Dónde se cría
        filas.getOrNull(3)?.let { fila ->
            FilaVisual(
                fila, Icons.Filled.WaterDrop,
                listOf(
                    FotoDef(R.drawable.cria_01_agua_potable, null, "Agua limpia guardada", Icons.Filled.WaterDrop, 1.5f) to
                            FotoDef(R.drawable.cria_02_canal, null, "Canales con agua sucia", Icons.Filled.WaterDrop, 1.5f),
                    FotoDef(R.drawable.cria_06_llanta, null, "Llantas con agua", Icons.Filled.WaterDrop, 1.5f) to
                            FotoDef(R.drawable.cria_04_reservorio_sin_tapa, null, "Charcos y desagües", Icons.Filled.WaterDrop, 1.5f),
                    FotoDef(R.drawable.cria_08_macetas, null, "Platos de macetas", Icons.Filled.WaterDrop, 1.5f) to
                            FotoDef(R.drawable.cria_09_canaleta, null, "Canaletas con agua", Icons.Filled.WaterDrop, 1.5f)
                )
            )
        }

        // 5. Enfermedades
        filas.getOrNull(4)?.let { fila ->
            FilaVisual(
                fila, Icons.Filled.LocalHospital,
                listOf(
                    FotoDef(0, null, "Puede transmitir dengue", Icons.Filled.Warning, 2.2f, Color(0xFFFFE0E0), AlertaRoja) to
                            FotoDef(0, null, "No transmite dengue", Icons.Filled.CheckCircle, 2.2f, Color(0xFFDDEEFF), Azul700)
                )
            )
        }

        Text(
            "Fotos del zancudo común (Culex quinquefasciatus): CDC, Public Health Image Library " +
                    "(phil.cdc.gov). Créditos: Lauren Bishop y Amy E. Lockwood. Imágenes de dominio público. " +
                    "Necesitan internet para verse.",
            style = MaterialTheme.typography.bodySmall,
            color = c.onSurfaceVariant
        )

        AvisoCard("No se puede asegurar la especie solo mirando al mosquito. Ante la duda, elimina los criaderos y, si hay fiebre, consulta en un establecimiento de salud.")

        Desplegable(titulo = "Cómo protegerte de las picaduras", icono = Icons.Filled.Shield, abiertoInicial = true) {
            Contenido.consejosContraPicaduras.forEach { Vineta(it) }
        }

        Text(
            "Información educativa de carácter general. Ante cualquier duda sobre tu salud, consulta a un profesional de salud.",
            style = MaterialTheme.typography.bodySmall,
            color = c.onSurfaceVariant
        )
    }
}
