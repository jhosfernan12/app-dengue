package com.ucsm.dengueapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** Cuestionario en línea (Google Formularios) para probar los conocimientos sobre el dengue. */
const val URL_CUESTIONARIO =
    "https://docs.google.com/forms/d/e/1FAIpQLSdI4Yf_HNt6nty5-U2c4xADLdQNaxiKPs0rYvH_qfJOdT8UJw/viewform?usp=header"

private const val PAUSA_CARRUSEL_MS = 3500L

@Composable
fun PantallaQuiz(onVolver: () -> Unit) {
    val context = LocalContext.current
    val c = MaterialTheme.colorScheme
    val fotos = ContenidoGaleria.fotos.take(6)
    val estado = rememberPagerState { fotos.size }

    // El carrusel avanza solo cada pocos segundos
    LaunchedEffect(Unit) {
        while (true) {
            delay(PAUSA_CARRUSEL_MS)
            estado.animateScrollToPage((estado.currentPage + 1) % fotos.size)
        }
    }

    Pagina("Prueba tus conocimientos", onVolver) {
        Cabecera(
            "Prueba tus conocimientos",
            "Mira las imágenes y pon a prueba lo que aprendiste sobre el dengue."
        )

        // ----- Carrusel de fotos -----
        HorizontalPager(
            state = estado,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 12.dp
        ) { pagina ->
            val foto = fotos[pagina]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(28.dp))
            ) {
                Image(
                    painter = painterResource(foto.recurso),
                    contentDescription = foto.titulo,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC061A3A)))
                        )
                )
                Text(
                    foto.titulo,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(18.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // ----- Puntos del carrusel -----
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(fotos.size) { i ->
                val activo = i == estado.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (activo) 10.dp else 7.dp)
                        .clip(CircleShape)
                        .background(if (activo) c.primary else c.outlineVariant)
                )
            }
        }

        // ----- Botón que abre el cuestionario -----
        Button(
            onClick = {
                UsageLog.increment(context, UsageLog.QUIZZES)
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(URL_CUESTIONARIO)))
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "No se encontró un navegador para abrir el enlace", Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.Filled.School, contentDescription = null, modifier = Modifier.size(24.dp))
            Text(
                "  Probar mis conocimientos",
                style = MaterialTheme.typography.titleMedium
            )
        }

        // ----- Texto explicativo -----
        Text(
            "Este es el enlace a un cuestionario para probar tus conocimientos sobre el dengue. " +
                    "Se abrirá en tu navegador y necesitas conexión a internet.",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            color = c.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
