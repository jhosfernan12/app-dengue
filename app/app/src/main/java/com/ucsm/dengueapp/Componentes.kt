package com.ucsm.dengueapp

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ucsm.dengueapp.ui.theme.Azul500
import com.ucsm.dengueapp.ui.theme.Azul700
import com.ucsm.dengueapp.ui.theme.AzulBarra
import com.ucsm.dengueapp.ui.theme.AzulMarino
import com.ucsm.dengueapp.ui.theme.LocalCambiarTema
import com.ucsm.dengueapp.ui.theme.LocalModoOscuro

// ---------- Navegación (barra inferior) ----------

/** Pantalla actual y función para cambiar de pantalla. La provee AppDengue(). */
class Navegacion(val actual: Pantalla, val ir: (Pantalla) -> Unit)

val LocalNavegacion = compositionLocalOf<Navegacion?> { null }

private class Destino(val pantalla: Pantalla, val etiqueta: String, val icono: ImageVector)

private val destinos = listOf(
    Destino(Pantalla.Inicio, "Inicio", Icons.Filled.Home),
    Destino(Pantalla.Galeria, "Galería", Icons.Filled.PhotoLibrary),
    Destino(Pantalla.Mosquito, "Escanear", Icons.Filled.CameraAlt),
    Destino(Pantalla.ListaVideos, "Videos", Icons.Filled.PlayCircle),
    Destino(Pantalla.Mapa, "Mapa", Icons.Filled.Place)
)

/** Barra inferior de las 5 secciones principales. En las demás pantallas no se muestra. */
@Composable
fun BarraInferior() {
    val nav = LocalNavegacion.current ?: return
    if (destinos.none { it.pantalla == nav.actual }) return

    // La barra no crece con el tamaño de letra elegido: así siempre cabe en una fila
    val d = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(d.density, fontScale = 1f)) {
        Surface(
            color = AzulBarra,
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
            shadowElevation = 16.dp
        ) {
            NavigationBar(containerColor = Color.Transparent, tonalElevation = 0.dp) {
                destinos.forEach { destino ->
                    val seleccionado = nav.actual == destino.pantalla
                    NavigationBarItem(
                        selected = seleccionado,
                        onClick = { if (!seleccionado) nav.ir(destino.pantalla) },
                        icon = { Icon(destino.icono, contentDescription = null) },
                        label = {
                            Text(
                                destino.etiqueta,
                                maxLines = 1,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AzulMarino,
                            selectedTextColor = Color.White,
                            indicatorColor = Color.White,
                            unselectedIconColor = Color.White.copy(alpha = 0.75f),
                            unselectedTextColor = Color.White.copy(alpha = 0.75f)
                        )
                    )
                }
            }
        }
    }
}

// ---------- Formas y piezas decorativas ----------

/** Fondo con la parte de abajo ondulada, como en las pantallas de bienvenida del diseño de referencia. */
val OndaInferior = GenericShape { s, _ ->
    moveTo(0f, 0f)
    lineTo(s.width, 0f)
    lineTo(s.width, s.height * 0.84f)
    cubicTo(s.width * 0.76f, s.height * 1.02f, s.width * 0.40f, s.height * 0.70f, 0f, s.height * 0.94f)
    close()
}

fun degradadoAzul(oscuro: Boolean): Brush = Brush.linearGradient(
    if (oscuro) listOf(Color(0xFF0B2347), Color(0xFF0F4C9A))
    else listOf(AzulMarino, Azul700, Azul500)
)

/** Dos círculos translúcidos de adorno, para dar profundidad a los fondos azules. */
fun Modifier.burbujas(): Modifier = this.drawBehind {
    drawCircle(
        Color.White.copy(alpha = 0.08f),
        radius = size.minDimension * 0.55f,
        center = Offset(size.width * 0.92f, size.height * 0.05f)
    )
    drawCircle(
        Color.White.copy(alpha = 0.06f),
        radius = size.minDimension * 0.35f,
        center = Offset(size.width * 0.04f, size.height * 0.85f)
    )
}

/** Foto en círculo con borde blanco y sombra (ícono de la app). */
@Composable
fun FotoCircular(
    @DrawableRes recurso: Int,
    tamano: Dp,
    descripcion: String?,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(recurso),
        contentDescription = descripcion,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(tamano)
            .shadow(10.dp, CircleShape)
            .border(3.dp, Color.White, CircleShape)
            .clip(CircleShape)
    )
}

/** Franja con una foto recortada, para la parte de arriba de las pantallas informativas. */
@Composable
fun FotoBanner(
    @DrawableRes recurso: Int,
    descripcion: String,
    pie: String? = null,
    alto: Dp = 180.dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(alto)
            .clip(RoundedCornerShape(24.dp))
    ) {
        Image(
            painter = painterResource(recurso),
            contentDescription = descripcion,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = BiasAlignment(0f, -0.15f)
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xAA061A3A))))
        )
        if (pie != null) {
            Text(
                pie,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                color = Color.White,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

// ---------- Barra superior y página ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(titulo: String, onVolver: (() -> Unit)?) {
    val oscuro = LocalModoOscuro.current
    val cambiarTema = LocalCambiarTema.current

    CenterAlignedTopAppBar(
        title = {
            Text(
                titulo,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (onVolver != null) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
        },
        actions = {
            IconButton(onClick = { cambiarTema(!oscuro) }) {
                Icon(
                    if (oscuro) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                    contentDescription = if (oscuro) "Cambiar a modo claro" else "Cambiar a modo oscuro"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AzulBarra,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}

/**
 * Pantalla base. Con [conBarraSuperior] = false (pantalla de inicio) el contenido ocupa todo el ancho
 * y debe encargarse él mismo del espacio de la barra de estado (ver HeroInicio).
 */
@Composable
fun Pagina(
    titulo: String,
    onVolver: (() -> Unit)?,
    conBarraSuperior: Boolean = true,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = { if (conBarraSuperior) BarraSuperior(titulo, onVolver) },
        bottomBar = { BarraInferior() },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = if (conBarraSuperior) {
            ScaffoldDefaults.contentWindowInsets
        } else {
            WindowInsets(0, 0, 0, 0)
        }
    ) { padding ->
        val base = Modifier
            .padding(padding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
        if (conBarraSuperior) {
            Column(
                modifier = base.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = contenido
            )
        } else {
            Column(modifier = base, content = contenido)
        }
    }
}

// ---------- Tarjetas y piezas visuales ----------

@Composable
fun AvisoCard(texto: String) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Text(
                texto,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

/** Tarjeta de encabezado azul con degradado y, si se quiere, una foto circular a la derecha. */
@Composable
fun Cabecera(titulo: String, subtitulo: String, @DrawableRes imagen: Int? = null) {
    val oscuro = LocalModoOscuro.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(degradadoAzul(oscuro))
            .burbujas()
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    titulo,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
                Text(
                    subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.92f)
                )
            }
            if (imagen != null) {
                FotoCircular(imagen, 80.dp, null)
            }
        }
    }
}

/** Título de bloque ("Explora", "Galería"…) con un enlace opcional a la derecha. */
@Composable
fun TituloSeccion(texto: String, accion: String? = null, onAccion: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            texto,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (accion != null) {
            TextButton(onClick = onAccion) { Text(accion) }
        }
    }
}

/** Botón cuadrado de la pantalla de inicio: ícono en un recuadro de color, título y una línea de detalle. */
@Composable
fun Baldosa(
    texto: String,
    detalle: String?,
    icono: ImageVector,
    acento: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val c = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = c.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, c.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    texto,
                    color = c.onSurface,
                    style = MaterialTheme.typography.titleSmall
                )
                if (detalle != null) {
                    Text(
                        detalle,
                        color = c.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun Desplegable(
    titulo: String,
    icono: ImageVector? = null,
    colorEncabezado: Color = MaterialTheme.colorScheme.primaryContainer,
    colorTexto: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    abiertoInicial: Boolean = false,
    contenido: @Composable ColumnScope.() -> Unit
) {
    var abierto by rememberSaveable { mutableStateOf(abiertoInicial) }
    val giro by animateFloatAsState(if (abierto) 180f else 0f, label = "giro")

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorEncabezado)
                    .clickable { abierto = !abierto }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (icono != null) {
                    Icon(icono, contentDescription = null, tint = colorTexto)
                }
                Text(
                    titulo,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold,
                    color = colorTexto,
                    style = MaterialTheme.typography.titleSmall
                )
                Icon(
                    Icons.Filled.ExpandMore,
                    contentDescription = if (abierto) "Contraer" else "Expandir",
                    tint = colorTexto,
                    modifier = Modifier.rotate(giro)
                )
            }
            AnimatedVisibility(visible = abierto) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    content = contenido
                )
            }
        }
    }
}

@Composable
fun Vineta(texto: String, color: Color = MaterialTheme.colorScheme.primary) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 7.dp)
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(texto, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun Etiqueta(texto: String, fondo: Color, color: Color) {
    Text(
        texto,
        color = color,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(fondo)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
fun SeccionInformativa(secciones: List<SeccionInfo>) {
    secciones.forEachIndexed { i, seccion ->
        Desplegable(titulo = seccion.titulo, abiertoInicial = i == 0) {
            seccion.contenido.forEach { Vineta(it) }
        }
    }
}
