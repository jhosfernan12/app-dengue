package com.ucsm.dengueapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ucsm.dengueapp.ui.theme.AlertaRoja
import com.ucsm.dengueapp.ui.theme.Azul700
import com.ucsm.dengueapp.ui.theme.AzulClaro
import com.ucsm.dengueapp.ui.theme.AzulOscuro
import com.ucsm.dengueapp.ui.theme.LocalModoOscuro
import com.ucsm.dengueapp.ui.theme.NaranjaAcento
import com.ucsm.dengueapp.ui.theme.NaranjaFebril
import com.ucsm.dengueapp.ui.theme.RojoAcento
import com.ucsm.dengueapp.ui.theme.RojoClaro
import com.ucsm.dengueapp.ui.theme.RojoOscuro
import com.ucsm.dengueapp.ui.theme.VerdeAcento
import com.ucsm.dengueapp.ui.theme.VerdeFuerte

// ---------- Mosquito del dengue vs. zancudo común ----------

@Composable
fun PantallaComparacion(onVolver: () -> Unit) {
    val c = MaterialTheme.colorScheme
    val textoVoz = "Mosquito del dengue frente a zancudo común. " +
            Contenido.comparacion.joinToString(". ") {
                "${it.aspecto}. Mosquito del dengue: ${it.dengue} Zancudo común: ${it.comun}"
            } + ". No se puede asegurar la especie solo mirando. Ante la duda, elimina criaderos y consulta a un establecimiento de salud."

    Pagina("Dengue vs. zancudo común", onVolver) {
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
                            "pero tienen diferencias en su aspecto, en cuándo pican y en dónde se crían.",
                    color = c.onSecondaryContainer
                )
            }
        }

        BotonEscuchar(textoVoz)

        Contenido.comparacion.forEach { fila ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = c.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        fila.aspecto,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = c.primary
                    )
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
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Etiqueta("Mosquito del dengue", AlertaRoja, Color.White)
                            Text(fila.dengue, color = RojoOscuro, style = MaterialTheme.typography.bodySmall)
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(14.dp))
                                .background(AzulClaro)
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Etiqueta("Zancudo común", Azul700, Color.White)
                            Text(fila.comun, color = AzulOscuro, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        AvisoCard("No se puede asegurar la especie solo mirando al mosquito. Ante la duda, elimina los criaderos y, si hay fiebre, consulta en un establecimiento de salud.")

        Desplegable(titulo = "Cómo protegerte de las picaduras", icono = Icons.Filled.Shield, abiertoInicial = true) {
            Contenido.consejosContraPicaduras.forEach { Vineta(it) }
        }

        Text(
            "Información educativa general. Debe ser validada por profesionales de salud antes de usarla con participantes.",
            style = MaterialTheme.typography.bodySmall,
            color = c.onSurfaceVariant
        )
    }
}

// ---------- Etapas de la enfermedad ----------

@Composable
fun PantallaEtapas(onVolver: () -> Unit) {
    val c = MaterialTheme.colorScheme
    val oscuro = LocalModoOscuro.current
    val etapas = Contenido.etapas

    // "fuertes": fondos con texto blanco. "acentos": texto y detalles sobre la superficie de la tarjeta.
    val fuertes = listOf(NaranjaFebril, AlertaRoja, VerdeFuerte)
    val acentos = if (oscuro) listOf(NaranjaAcento, RojoAcento, VerdeAcento) else fuertes

    var sel by rememberSaveable { mutableStateOf(0) }
    val etapa = etapas[sel]
    val color = fuertes[sel]
    val acento = acentos[sel]

    Pagina("Etapas de la enfermedad", onVolver) {
        Text(
            "Toca una etapa para ver qué suele pasar y qué hacer en cada una.",
            style = MaterialTheme.typography.bodyMedium
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            etapas.forEachIndexed { i, e ->
                val activo = i == sel
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (activo) fuertes[i] else acentos[i].copy(alpha = 0.15f))
                        .clickable { sel = i }
                        .padding(vertical = 12.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "${i + 1}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (activo) Color.White else acentos[i]
                    )
                    Text(
                        e.nombreCorto,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        color = if (activo) Color.White else acentos[i]
                    )
                }
            }
        }

        BotonEscuchar(
            etapa.nombre + ". " + etapa.cuando + ". " + etapa.resumen +
                    " Qué suele pasar: " + etapa.quePasa.joinToString(". ") +
                    ". Qué hacer: " + etapa.queHacer.joinToString(". ") +
                    ". " + etapa.cuandoAcudir
        )

        key(sel) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = color)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        etapa.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Etiqueta(etapa.cuando, Color.White, color)
                    Text(etapa.resumen, color = Color.White, fontWeight = FontWeight.Medium)
                }
            }

            Desplegable(
                titulo = "Qué suele pasar",
                icono = Icons.Filled.Info,
                colorEncabezado = acento.copy(alpha = 0.15f),
                colorTexto = acento,
                abiertoInicial = true
            ) {
                etapa.quePasa.forEach { Vineta(it, acento) }
            }

            Desplegable(
                titulo = "Qué hacer",
                icono = Icons.Filled.Shield,
                colorEncabezado = acento.copy(alpha = 0.15f),
                colorTexto = acento,
                abiertoInicial = true
            ) {
                etapa.queHacer.forEach { Vineta(it, acento) }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = c.errorContainer)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = c.error)
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Cuándo acudir",
                            fontWeight = FontWeight.Bold,
                            color = c.onErrorContainer
                        )
                        Text(etapa.cuandoAcudir, color = c.onErrorContainer)
                    }
                }
            }
        }

        Text(
            "Las etapas son orientativas: cada persona evoluciona distinto. Solo el personal de salud puede evaluarte. " +
                    "Información educativa que debe ser validada por profesionales de salud.",
            style = MaterialTheme.typography.bodySmall,
            color = c.onSurfaceVariant
        )
    }
}