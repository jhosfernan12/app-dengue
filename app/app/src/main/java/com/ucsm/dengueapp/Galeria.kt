package com.ucsm.dengueapp

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Imagen de la galería.
 * [esquema] = true para infografías e ilustraciones (se muestran completas, sin recortar);
 * false para fotografías (se recortan en cuadrado en la cuadrícula y se ven completas al abrirlas).
 */
data class ImagenGaleria(
    val id: String,
    @DrawableRes val recurso: Int,
    val titulo: String,
    val descripcion: String,
    val credito: String,
    val esquema: Boolean = false
)

object ContenidoGaleria {
    private const val ILUSTRATIVA = "Imagen ilustrativa"

    val imagenes = listOf(
        ImagenGaleria(
            "g1", R.drawable.gal_01_perfil,
            "Aedes aegypti de perfil",
            "Cuerpo oscuro con manchas blancas y patas con bandas blancas. Ese patrón es lo que ayuda a distinguirlo de un zancudo común.",
            ILUSTRATIVA
        ),
        ImagenGaleria(
            "g2", R.drawable.gal_02_primer_plano,
            "Primer plano del cuerpo",
            "Detalle del tórax y del abdomen: manchas y líneas blancas sobre un fondo oscuro.",
            ILUSTRATIVA
        ),
        ImagenGaleria(
            "g3", R.drawable.gal_03_sobre_la_piel,
            "Posado sobre la piel",
            "El Aedes aegypti pica sobre todo de día, temprano en la mañana y al atardecer.",
            ILUSTRATIVA
        ),
        ImagenGaleria(
            "g4", R.drawable.gal_04_en_reposo,
            "Patas largas y delgadas",
            "Se apoya sobre patas largas y finas. Vuela bajo y casi sin hacer ruido.",
            ILUSTRATIVA
        ),
        ImagenGaleria(
            "g5", R.drawable.gal_05_cabeza,
            "Cabeza y probóscide",
            "Con la probóscide (la \"trompa\") perfora la piel para alimentarse de sangre. Solo las hembras pican.",
            ILUSTRATIVA
        ),
        ImagenGaleria(
            "g6", R.drawable.gal_06_patas,
            "Patas con bandas blancas",
            "Las bandas blancas en las patas son una de las marcas más fáciles de reconocer.",
            ILUSTRATIVA
        ),
        ImagenGaleria(
            "g7", R.drawable.gal_07_aedes_piel_a,
            "Aedes sobre la piel (1)",
            "Fotografía de un mosquito Aedes posado sobre la piel.",
            "Crédito: © INHP / G. McCormack"
        ),
        ImagenGaleria(
            "g8", R.drawable.gal_08_aedes_piel_b,
            "Aedes sobre la piel (2)",
            "Fotografía de mosquitos Aedes posados sobre la piel.",
            "Crédito: © INHP / G. McCormack"
        ),
        ImagenGaleria(
            "e1", R.drawable.gal_esq_01_caracteristicas,
            "Mosquito del dengue: cómo reconocerlo",
            "Mide menos de 1 cm, vuela bajo, tiene alas y cuerpo de color oscuro, tres pares de patas y el abdomen rayado.",
            "Fuente: mdsaude.com",
            esquema = true
        ),
        ImagenGaleria(
            "e2", R.drawable.gal_esq_02_macho_hembra,
            "Aedes aegypti: macho y hembra",
            "El macho tiene antenas plumosas, mucho más vistosas que las de la hembra. Solo la hembra pica.",
            "Ilustración científica",
            esquema = true
        ),
        ImagenGaleria(
            "e3", R.drawable.gal_esq_03_aegypti_albopictus,
            "Aedes aegypti vs. Aedes albopictus",
            "Mira el tórax: el aegypti tiene dos líneas rectas en el centro y dos curvas a los lados; el albopictus tiene una sola línea en el centro.",
            "Fuente: mdsaude.com",
            esquema = true
        )
    )

    val fotos get() = imagenes.filter { !it.esquema }
    val esquemas get() = imagenes.filter { it.esquema }
}

// ---------- Pantalla ----------

@Composable
fun PantallaGaleria(onVolver: () -> Unit) {
    val c = MaterialTheme.colorScheme
    // 0 = todas, 1 = fotos, 2 = esquemas
    var filtro by rememberSaveable { mutableIntStateOf(0) }
    var visor by rememberSaveable { mutableStateOf<String?>(null) }   // id de la imagen abierta

    val fotos = if (filtro != 2) ContenidoGaleria.fotos else emptyList()
    val esquemas = if (filtro != 1) ContenidoGaleria.esquemas else emptyList()
    val visibles = fotos + esquemas

    Pagina("Galería", onVolver) {
        Cabecera(
            "Galería del mosquito",
            "Mira cómo es el Aedes aegypti: fotos y esquemas para reconocerlo.",
            imagen = R.drawable.img_mosquito_foto
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Todas", "Fotos", "Esquemas").forEachIndexed { i, texto ->
                FilterChip(
                    selected = filtro == i,
                    onClick = { filtro = i },
                    label = { Text(texto) },
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = c.primary,
                        selectedLabelColor = c.onPrimary
                    )
                )
            }
        }

        if (fotos.isNotEmpty()) {
            if (filtro == 0) TituloSeccion("Fotografías")
            fotos.chunked(2).forEach { fila ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    fila.forEach { img ->
                        MiniaturaFoto(img, Modifier.weight(1f)) { visor = img.id }
                    }
                    if (fila.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }

        if (esquemas.isNotEmpty()) {
            if (filtro == 0) TituloSeccion("Esquemas e infografías")
            esquemas.forEach { img -> TarjetaEsquema(img) { visor = img.id } }
        }

        Text(
            "Material educativo de apoyo: algunas imágenes son ilustrativas y no sirven para diagnosticar. " +
                    "El crédito aparece en cada imagen.",
            style = MaterialTheme.typography.bodySmall,
            color = c.onSurfaceVariant
        )
    }

    val abierta = visibles.indexOfFirst { it.id == visor }
    if (abierta >= 0) {
        VisorGaleria(lista = visibles, inicio = abierta, onCerrar = { visor = null })
    }
}

@Composable
private fun MiniaturaFoto(img: ImagenGaleria, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(img.recurso),
            contentDescription = img.titulo,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xB0061A3A)), startY = 200f))
        )
        Text(
            img.titulo,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 2
        )
    }
}

@Composable
private fun TarjetaEsquema(img: ImagenGaleria, onClick: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = c.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, c.outlineVariant)
    ) {
        Column {
            // Fondo blanco fijo: los esquemas tienen fondo claro y así se ven bien también en modo oscuro
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(img.recurso),
                    contentDescription = img.titulo,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )
            }
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(img.titulo, style = MaterialTheme.typography.titleSmall, color = c.onSurface)
                Text(img.credito, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
            }
        }
    }
}

// ---------- Visor a pantalla completa ----------

@Composable
fun VisorGaleria(lista: List<ImagenGaleria>, inicio: Int, onCerrar: () -> Unit) {
    val estado = rememberPagerState(initialPage = inicio) { lista.size }

    Dialog(
        onDismissRequest = onCerrar,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xF2071226))
                .systemBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 8.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${estado.currentPage + 1} / ${lista.size}",
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.labelLarge
                )
                IconButton(onClick = onCerrar) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = Color.White)
                }
            }

            HorizontalPager(
                state = estado,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pagina ->
                val img = lista[pagina]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(img.recurso),
                            contentDescription = img.titulo,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        img.titulo,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        img.descripcion,
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        img.credito,
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }

            // Puntos indicadores
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                lista.indices.forEach { i ->
                    Box(
                        Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (i == estado.currentPage) 8.dp else 6.dp)
                            .background(
                                if (i == estado.currentPage) Color.White else Color.White.copy(alpha = 0.35f),
                                CircleShape
                            )
                    )
                }
            }
        }
    }
}
