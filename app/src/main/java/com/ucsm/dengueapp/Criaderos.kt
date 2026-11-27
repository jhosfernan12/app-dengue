package com.ucsm.dengueapp

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ucsm.dengueapp.ui.theme.AmbarClaro
import com.ucsm.dengueapp.ui.theme.AmbarOscuro
import com.ucsm.dengueapp.ui.theme.RojoClaro
import com.ucsm.dengueapp.ui.theme.RojoOscuro
import com.ucsm.dengueapp.ui.theme.VerdeClaro
import com.ucsm.dengueapp.ui.theme.VerdeOk

// ---------- Datos: zonas donde puede haber criaderos ----------

enum class EstadoZona { RIESGO, CUIDADO, PROTEGIDO }

data class ZonaCriadero(
    @DrawableRes val foto: Int,
    val titulo: String,
    val estado: EstadoZona,
    val consejo: String
)

object ZonasCriadero {
    // Contenido educativo general (REVISAR con Enfermería antes de publicar)
    val todas = listOf(
        ZonaCriadero(
            R.drawable.cria_01_agua_potable, "Agua potable", EstadoZona.CUIDADO,
            "Guarda el agua de consumo en recipientes limpios y siempre tapados."
        ),
        ZonaCriadero(
            R.drawable.cria_02_canal, "Canal de agua", EstadoZona.RIESGO,
            "Si el agua se queda quieta, límpialo y retira los restos para que corra."
        ),
        ZonaCriadero(
            R.drawable.cria_03_recipientes, "Recipientes de agua a la intemperie", EstadoZona.RIESGO,
            "Voltéalos, tápalos o cambia el agua y lávalos cada semana."
        ),
        ZonaCriadero(
            R.drawable.cria_04_reservorio_sin_tapa, "Reservorio de agua limpia sin tapa", EstadoZona.RIESGO,
            "Tápalo bien. Un reservorio abierto es un criadero muy frecuente."
        ),
        ZonaCriadero(
            R.drawable.cria_05_reservorio_con_tapa, "Reservorio de agua limpia con tapa", EstadoZona.PROTEGIDO,
            "Está protegido: mantenlo siempre tapado y lávalo con regularidad."
        ),
        ZonaCriadero(
            R.drawable.cria_06_llanta, "Llanta con agua de lluvia", EstadoZona.RIESGO,
            "Elimínala, voltéala o guárdala bajo techo."
        ),
        ZonaCriadero(
            R.drawable.cria_07_vegetacion, "Área de vegetación que acumula agua", EstadoZona.RIESGO,
            "Retira el agua acumulada y mantén el jardín limpio, sin recipientes olvidados."
        ),
        ZonaCriadero(
            R.drawable.cria_08_macetas, "Macetas o floreros", EstadoZona.RIESGO,
            "Cambia el agua y lava el platito. Mejor usa arena húmeda."
        ),
        ZonaCriadero(
            R.drawable.cria_09_canaleta, "Canaleta de techo", EstadoZona.RIESGO,
            "Límpiala para que la lluvia corra y no se estanque."
        ),
        ZonaCriadero(
            R.drawable.cria_10_objeto_abandonado, "Objeto abandonado que acumula agua de lluvia", EstadoZona.RIESGO,
            "Elimínalo o voltéalo para que no junte agua."
        )
    )
}

// ---------- Pantalla: Control de criadero ----------

@Composable
private fun TarjetaZona(zona: ZonaCriadero, revisada: Boolean, onCambio: (Boolean) -> Unit) {
    val c = MaterialTheme.colorScheme
    val textoEstado: String
    val fondoEstado = when (zona.estado) {
        EstadoZona.RIESGO -> RojoClaro
        EstadoZona.CUIDADO -> AmbarClaro
        EstadoZona.PROTEGIDO -> VerdeClaro
    }
    val colorEstado = when (zona.estado) {
        EstadoZona.RIESGO -> RojoOscuro
        EstadoZona.CUIDADO -> AmbarOscuro
        EstadoZona.PROTEGIDO -> VerdeOk
    }
    textoEstado = when (zona.estado) {
        EstadoZona.RIESGO -> "Posible criadero"
        EstadoZona.CUIDADO -> "Con cuidado"
        EstadoZona.PROTEGIDO -> "Protegido"
    }

    Card(
        onClick = { onCambio(!revisada) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = if (revisada) VerdeClaro else c.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, if (revisada) VerdeOk else c.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(zona.foto),
                contentDescription = zona.titulo,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    zona.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = c.onSurface
                )
                Etiqueta(textoEstado, fondoEstado, colorEstado)
                Text(
                    zona.consejo,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.onSurfaceVariant
                )
            }
            Checkbox(checked = revisada, onCheckedChange = null)
        }
    }
}

@Composable
fun PantallaChecklist(onVolver: () -> Unit) {
    val context = LocalContext.current
    val c = MaterialTheme.colorScheme
    val zonas = ZonasCriadero.todas
    val marcadas = remember { mutableStateListOf<Int>() }
    val total = zonas.size

    Pagina("Control de criadero", onVolver) {
        Cabecera(
            "Control de criadero",
            "Identifica en tu casa las zonas con posibles criaderos del mosquito.",
            R.drawable.cria_03_recipientes
        )
        Text(
            "Compara cada foto con lo que ves en tu hogar y marca las zonas que ya revisaste. " +
                    "Hazlo una vez por semana.",
            style = MaterialTheme.typography.bodyMedium
        )
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = c.primaryContainer)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "${marcadas.size} de $total zonas revisadas",
                    fontWeight = FontWeight.Bold,
                    color = c.onPrimaryContainer
                )
                LinearProgressIndicator(
                    progress = { if (total == 0) 0f else marcadas.size.toFloat() / total },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = c.primary,
                    trackColor = c.surface
                )
            }
        }
        zonas.forEachIndexed { i, zona ->
            TarjetaZona(zona, i in marcadas) { marcar ->
                val yaEsta = i in marcadas
                if (marcar && !yaEsta) marcadas.add(i)
                if (!marcar && yaEsta) marcadas.remove(i)
            }
        }
        Button(
            onClick = {
                UsageLog.increment(context, UsageLog.REVISIONES)
                Rutina.registrarRevision(context)
                onVolver()
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        ) { Text("Guardar control") }
    }
}
