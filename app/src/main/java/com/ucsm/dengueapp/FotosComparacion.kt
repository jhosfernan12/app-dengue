package com.ucsm.dengueapp

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ucsm.dengueapp.ui.theme.AlertaRoja
import com.ucsm.dengueapp.ui.theme.Azul700

/**
 * Fotos de la comparación. La foto del zancudo común es opcional: si el archivo
 * res/drawable-nodpi/zancudo_comun.jpg existe, se muestran las dos lado a lado;
 * si no, solo la foto del mosquito del dengue.
 */
@Composable
fun FotosComparacion() {
    val context = LocalContext.current
    val c = MaterialTheme.colorScheme
    val idComun = remember {
        context.resources.getIdentifier("zancudo_comun", "drawable", context.packageName)
    }

    if (idComun == 0) {
        FotoBanner(
            R.drawable.img_mosquito_foto,
            "Fotografía de un mosquito Aedes con manchas blancas en el cuerpo y bandas blancas en las patas",
            pie = "Fíjate en las bandas blancas de las patas"
        )
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TarjetaFotoMosquito(
                R.drawable.img_mosquito_foto,
                "Mosquito del dengue",
                "Aedes aegypti",
                AlertaRoja,
                Modifier.weight(1f)
            )
            TarjetaFotoMosquito(
                idComun,
                "Zancudo común",
                "Culex",
                Azul700,
                Modifier.weight(1f)
            )
        }
        Text(
            "Foto del zancudo común: Lauren Bishop / CDC.",
            style = MaterialTheme.typography.bodySmall,
            color = c.onSurfaceVariant
        )
    }
}

@Composable
private fun TarjetaFotoMosquito(
    @DrawableRes foto: Int,
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
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, c.outlineVariant)
    ) {
        Column {
            Image(
                painter = painterResource(foto),
                contentDescription = "Fotografía de $titulo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = acento
                )
                Text(
                    nombre,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.onSurfaceVariant
                )
            }
        }
    }
}
