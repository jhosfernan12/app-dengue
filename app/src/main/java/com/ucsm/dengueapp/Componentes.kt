package com.ucsm.dengueapp

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ucsm.dengueapp.ui.theme.Azul700
import com.ucsm.dengueapp.ui.theme.AzulProfundo
import com.ucsm.dengueapp.ui.theme.LocalCambiarTema
import com.ucsm.dengueapp.ui.theme.LocalModoOscuro
import com.ucsm.dengueapp.ui.theme.Teal700
import com.ucsm.dengueapp.ui.theme.TealOscuro

// ---------- Barra superior y página ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(titulo: String, onVolver: (() -> Unit)?) {
    val c = MaterialTheme.colorScheme
    val oscuro = LocalModoOscuro.current
    val cambiarTema = LocalCambiarTema.current
    val fondo = if (oscuro) c.primaryContainer else c.primary
    val texto = if (oscuro) c.onPrimaryContainer else c.onPrimary

    TopAppBar(
        title = { Text(titulo, fontWeight = FontWeight.SemiBold) },
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
            containerColor = fondo,
            titleContentColor = texto,
            navigationIconContentColor = texto,
            actionIconContentColor = texto
        )
    )
}

@Composable
fun Pagina(titulo: String, onVolver: (() -> Unit)?, contenido: @Composable ColumnScope.() -> Unit) {
    Scaffold(
        topBar = { BarraSuperior(titulo, onVolver) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = contenido
        )
    }
}

// ---------- Tarjetas y piezas visuales ----------

@Composable
fun AvisoCard(texto: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
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

@Composable
fun Cabecera(titulo: String, subtitulo: String, @DrawableRes imagen: Int? = null) {
    val oscuro = LocalModoOscuro.current
    val colores = if (oscuro) listOf(TealOscuro, AzulProfundo) else listOf(Teal700, Azul700)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(colores))
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
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.92f)
                )
            }
            if (imagen != null) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(imagen),
                        contentDescription = null,
                        modifier = Modifier.size(66.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun Baldosa(
    texto: String,
    icono: ImageVector,
    fondo: Color,
    contenido: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxHeight().heightIn(min = 112.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = fondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icono, contentDescription = null, tint = contenido, modifier = Modifier.size(32.dp))
            Text(
                texto,
                color = contenido,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleSmall
            )
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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