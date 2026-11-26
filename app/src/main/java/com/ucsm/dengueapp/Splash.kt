package com.ucsm.dengueapp

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ucsm.dengueapp.ui.theme.Azul500
import com.ucsm.dengueapp.ui.theme.Azul700
import com.ucsm.dengueapp.ui.theme.AzulMarino
import com.ucsm.dengueapp.ui.theme.LocalModoOscuro
import kotlinx.coroutines.delay

const val NOMBRE_APP = "Dengue-Scan"
const val TITULO_APP =
    "APLICATIVO MÓVIL EDUCATIVO PARA LA PREVENCIÓN DEL DENGUE, SIGNOS DE ALARMA, " +
            "IDENTIFICACIÓN DEL VECTOR TRANSMISOR, MEDIANTE MODELO MACHINE LEARNING"

private const val DURACION_CARATULA_MS = 3500

/**
 * Carátula que se muestra al abrir la app: fondo azul a pantalla completa, la foto del mosquito
 * con ondas que se expanden, el nombre, la descripción y una barra de progreso.
 */
@Composable
fun PantallaCarga(onTerminar: () -> Unit) {
    val oscuro = LocalModoOscuro.current

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
        delay(DURACION_CARATULA_MS.toLong())
        onTerminar()
    }
    val opacidad by animateFloatAsState(if (visible) 1f else 0f, tween(900), label = "opacidad")
    val tamano by animateFloatAsState(if (visible) 1f else 0.7f, tween(900), label = "tamano")
    val progreso by animateFloatAsState(
        if (visible) 1f else 0f,
        tween(DURACION_CARATULA_MS, easing = LinearEasing),
        label = "progreso"
    )
    val pulso by rememberInfiniteTransition(label = "pulso").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing)),
        label = "pulso"
    )

    val fondo = if (oscuro) {
        Brush.verticalGradient(listOf(Color(0xFF071A38), Color(0xFF0B2F66)))
    } else {
        Brush.verticalGradient(listOf(AzulMarino, Azul700, Azul500))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(fondo)
            .burbujas()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onTerminar() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Foto con ondas que se expanden
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .drawBehind {
                        val radioBase = size.minDimension * 0.30f
                        val radioMaximo = size.minDimension * 0.50f
                        for (k in 0..2) {
                            val fase = (pulso + k / 3f) % 1f
                            drawCircle(
                                color = Color.White.copy(alpha = (1f - fase) * 0.35f),
                                radius = radioBase + fase * (radioMaximo - radioBase),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                FotoCircular(
                    recurso = R.drawable.img_mosquito_foto,
                    tamano = 176.dp,
                    descripcion = "Fotografía de un mosquito Aedes",
                    modifier = Modifier
                        .scale(tamano)
                        .alpha(opacidad)
                )
            }

            Text(
                "BIENVENIDO A",
                modifier = Modifier.alpha(opacidad),
                style = MaterialTheme.typography.labelLarge,
                letterSpacing = 4.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                NOMBRE_APP,
                modifier = Modifier.alpha(opacidad),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Text(
                TITULO_APP,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(opacidad),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.6.sp,
                color = Color.White.copy(alpha = 0.88f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(34.dp))
            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(6.dp)
                    .clip(CircleShape),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.25f)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Toca para continuar",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(18.dp))
            Text(
                "Información educativa. No diagnostica dengue ni reemplaza la evaluación de un profesional de salud.",
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(opacidad),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}
