package com.ucsm.dengueapp

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

data class SintomaFoto(@DrawableRes val foto: Int, val nombre: String)

object SintomasFotos {
    // La imagen de la tos (sint_05_tos) se usa solo en la prueba de conocimientos,
    // porque la tos no es un síntoma típico del dengue.
    val lista = listOf(
        SintomaFoto(R.drawable.sint_01_fiebre, "Fiebre"),
        SintomaFoto(R.drawable.sint_02_vomitos, "Vómitos"),
        SintomaFoto(R.drawable.sint_03_dolor_musculos, "Dolor en músculos y articulaciones"),
        SintomaFoto(R.drawable.sint_04_ronchas, "Ronchas o granos en la piel")
    )
}

@Composable
private fun TarjetaSintoma(s: SintomaFoto, modifier: Modifier = Modifier) {
    val c = MaterialTheme.colorScheme
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = c.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, c.outlineVariant)
    ) {
        Column {
            Image(
                painter = painterResource(s.foto),
                contentDescription = s.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.05f)
            )
            Text(
                s.nombre,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.titleSmall,
                color = c.onSurface,
                minLines = 2
            )
        }
    }
}

@Composable
fun PantallaSintomas(onVolver: () -> Unit, onIr: (Pantalla) -> Unit) {
    val c = MaterialTheme.colorScheme
    val seccionSintomas = Contenido.informacionGeneral.filter { it.titulo.startsWith("Síntomas") }

    Pagina("Síntomas del dengue", onVolver) {
        Cabecera(
            "Síntomas del dengue",
            "Reconócelos en imágenes. Si tienes fiebre, consulta temprano en un establecimiento de salud."
        )
        SintomasFotos.lista.chunked(2).forEach { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                fila.forEach { TarjetaSintoma(it, Modifier.weight(1f)) }
            }
        }
        BotonEscuchar(textoDe(seccionSintomas))
        SeccionInformativa(seccionSintomas)
        Text(
            "Estos síntomas pueden parecerse a los de otras enfermedades. " +
                    "Esta información es educativa y no reemplaza una consulta médica.",
            style = MaterialTheme.typography.bodySmall,
            color = c.onSurfaceVariant
        )
        Button(
            onClick = { onIr(Pantalla.SignosAlarma) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        ) { Text("Ver signos de alarma") }
        OutlinedButton(
            onClick = { onIr(Pantalla.Orientacion) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        ) { Text("Guía rápida: ¿cuándo acudir al centro de salud?") }
    }
}
