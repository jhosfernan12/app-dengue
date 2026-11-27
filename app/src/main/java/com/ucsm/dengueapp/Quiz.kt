package com.ucsm.dengueapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ucsm.dengueapp.ui.theme.AlertaRoja
import com.ucsm.dengueapp.ui.theme.AmbarClaro
import com.ucsm.dengueapp.ui.theme.AmbarOscuro
import com.ucsm.dengueapp.ui.theme.LocalModoOscuro
import com.ucsm.dengueapp.ui.theme.RojoClaro
import com.ucsm.dengueapp.ui.theme.VerdeClaro
import com.ucsm.dengueapp.ui.theme.VerdeOk

// ---------- Preguntas ----------

/**
 * [correctas] tiene un solo número en las preguntas de una respuesta
 * y varios en las de "marca todas las correctas".
 * [fotos], si existe, trae una imagen por opción (en el mismo orden).
 */
data class PreguntaQuiz(
    val enunciado: String,
    val opciones: List<String>,
    val correctas: Set<Int>,
    val explicacion: String,
    val fotos: List<Int>? = null
)

object BancoQuiz {
    // Contenido educativo general (REVISAR con Enfermería antes de publicar)
    val preguntas = listOf(
        PreguntaQuiz(
            enunciado = "¿Qué mosquito transmite el dengue?",
            opciones = listOf(
                "Aedes aegypti",
                "El zancudo común",
                "La mosca doméstica",
                "La pulga"
            ),
            correctas = setOf(0),
            explicacion = "El dengue se transmite por la picadura del mosquito Aedes aegypti. " +
                    "El zancudo común no transmite dengue."
        ),
        PreguntaQuiz(
            enunciado = "¿El dengue se contagia de persona a persona?",
            opciones = listOf(
                "Sí, por la tos y los estornudos",
                "Sí, al compartir alimentos",
                "No, solo por la picadura del mosquito infectado",
                "Sí, al tocar a una persona enferma"
            ),
            correctas = setOf(2),
            explicacion = "El dengue no se contagia de persona a persona, ni por el agua, " +
                    "ni por contacto con animales. Se transmite por la picadura del mosquito."
        ),
        PreguntaQuiz(
            enunciado = "¿Dónde pone sus huevos el Aedes aegypti?",
            opciones = listOf(
                "En ríos y en el mar",
                "En recipientes con agua limpia y estancada",
                "En la ropa y en las camas",
                "En lugares totalmente secos"
            ),
            correctas = setOf(1),
            explicacion = "Pone sus huevos en recipientes con agua limpia y estancada, " +
                    "dentro y alrededor de la casa."
        ),
        PreguntaQuiz(
            enunciado = "¿Cuáles de estos síntomas se presentan en el dengue?",
            opciones = listOf(
                "Fiebre",
                "Vómitos",
                "Dolor en músculos y articulaciones",
                "Ronchas o granos en la piel",
                "Tos"
            ),
            correctas = setOf(0, 1, 2, 3),
            explicacion = "La fiebre, los vómitos, el dolor de músculos y articulaciones y las ronchas " +
                    "o el sarpullido pueden presentarse en el dengue. La tos no es un síntoma típico.",
            fotos = listOf(
                R.drawable.sint_01_fiebre,
                R.drawable.sint_02_vomitos,
                R.drawable.sint_03_dolor_musculos,
                R.drawable.sint_04_ronchas,
                R.drawable.sint_05_tos
            )
        ),
        PreguntaQuiz(
            enunciado = "¿Cada cuánto conviene revisar la casa en busca de criaderos?",
            opciones = listOf(
                "Una vez al año",
                "Una vez por semana",
                "Solo cuando hay casos en el barrio",
                "Solo en verano"
            ),
            correctas = setOf(1),
            explicacion = "El mosquito completa su desarrollo en cerca de una semana. " +
                    "Revisar y eliminar el agua acumulada cada semana corta ese ciclo."
        ),
        PreguntaQuiz(
            enunciado = "¿Cuáles de estos lugares son posibles criaderos?",
            opciones = listOf(
                "Llanta con agua de lluvia",
                "Reservorio de agua sin tapa",
                "Macetas o floreros con agua",
                "Reservorio de agua con tapa"
            ),
            correctas = setOf(0, 1, 2),
            explicacion = "Las llantas, los reservorios sin tapa y los floreros con agua acumulan agua " +
                    "estancada. El reservorio bien tapado está protegido.",
            fotos = listOf(
                R.drawable.cria_06_llanta,
                R.drawable.cria_04_reservorio_sin_tapa,
                R.drawable.cria_08_macetas,
                R.drawable.cria_05_reservorio_con_tapa
            )
        ),
        PreguntaQuiz(
            enunciado = "Tienes fiebre y un dolor de barriga intenso que no se quita. ¿Qué debes hacer?",
            opciones = listOf(
                "Esperar unos días en casa",
                "Acudir de inmediato a un establecimiento de salud",
                "Tomar aspirina",
                "Esperar a que se pase solo"
            ),
            correctas = setOf(1),
            explicacion = "El dolor abdominal intenso y continuo es un signo de alarma. " +
                    "Acude de inmediato a un establecimiento de salud."
        ),
        PreguntaQuiz(
            enunciado = "Si sospechas dengue, ¿qué medicamentos debes evitar sin indicación médica?",
            opciones = listOf(
                "Aspirina o ibuprofeno",
                "Paracetamol",
                "Ninguno, todos son seguros"
            ),
            correctas = setOf(0),
            explicacion = "La aspirina y el ibuprofeno pueden aumentar el riesgo de sangrado. " +
                    "No te automediques y sigue siempre la indicación de un profesional de salud."
        ),
        PreguntaQuiz(
            enunciado = "¿Cuál de estos es un signo de alarma del dengue?",
            opciones = listOf(
                "Estornudos",
                "Sangrado de encías o de nariz",
                "Picazón en la garganta",
                "Dolor de oído"
            ),
            correctas = setOf(1),
            explicacion = "El sangrado de encías, nariz, orina o heces es un signo de alarma. " +
                    "Ante cualquier signo de alarma, acude de inmediato a un establecimiento de salud."
        ),
        PreguntaQuiz(
            enunciado = "¿Qué hace la función \"Identifica un mosquito\" de la app?",
            opciones = listOf(
                "Diagnostica si tienes dengue",
                "Orienta si la foto parece un Aedes, sin reemplazar a un profesional",
                "Elimina los mosquitos de tu casa",
                "Receta medicamentos"
            ),
            correctas = setOf(1),
            explicacion = "Es una función experimental y orientativa. No diagnostica dengue " +
                    "ni reemplaza la evaluación de un profesional de salud."
        )
    )
}

// ---------- Pantalla ----------

@Composable
fun PantallaQuiz(onVolver: () -> Unit, onIr: (Pantalla) -> Unit) {
    val context = LocalContext.current
    val c = MaterialTheme.colorScheme
    val preguntas = BancoQuiz.preguntas
    var indice by rememberSaveable { mutableIntStateOf(0) }
    var aciertos by rememberSaveable { mutableIntStateOf(0) }
    var terminado by rememberSaveable { mutableStateOf(false) }

    Pagina("Prueba tus conocimientos", onVolver) {
        if (terminado) {
            ResultadoQuiz(
                aciertos = aciertos,
                total = preguntas.size,
                onReintentar = {
                    indice = 0
                    aciertos = 0
                    terminado = false
                },
                onIr = onIr
            )
        } else {
            val pregunta = preguntas[indice]
            val multiple = pregunta.correctas.size > 1
            val seleccion = remember(indice) { mutableStateListOf<Int>() }
            var respondida by remember(indice) { mutableStateOf(false) }
            val acerto = seleccion.toSet() == pregunta.correctas
            val ultima = indice == preguntas.size - 1

            Text(
                "Pregunta ${indice + 1} de ${preguntas.size}",
                style = MaterialTheme.typography.labelLarge,
                color = c.primary
            )
            LinearProgressIndicator(
                progress = {
                    (indice + if (respondida) 1 else 0).toFloat() / preguntas.size
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = c.primary,
                trackColor = c.primaryContainer
            )
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = c.primaryContainer)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        pregunta.enunciado,
                        style = MaterialTheme.typography.titleMedium,
                        color = c.onPrimaryContainer
                    )
                    if (multiple) {
                        Text(
                            "Marca todas las respuestas correctas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.onPrimaryContainer
                        )
                    }
                }
            }

            pregunta.opciones.forEachIndexed { i, texto ->
                OpcionQuiz(
                    texto = texto,
                    foto = pregunta.fotos?.getOrNull(i),
                    marcada = i in seleccion,
                    multiple = multiple,
                    respondida = respondida,
                    esCorrecta = i in pregunta.correctas
                ) {
                    if (multiple) {
                        if (i in seleccion) seleccion.remove(i) else seleccion.add(i)
                    } else {
                        seleccion.clear()
                        seleccion.add(i)
                    }
                }
            }

            if (!respondida) {
                Button(
                    onClick = {
                        respondida = true
                        if (acerto) aciertos += 1
                    },
                    enabled = seleccion.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                ) { Text("Responder") }
            } else {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (acerto) VerdeClaro else AmbarClaro
                    )
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            if (acerto) "¡Correcto!" else "Casi. Mira la respuesta marcada en verde.",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (acerto) VerdeOk else AmbarOscuro
                        )
                        Text(
                            pregunta.explicacion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.onSurface
                        )
                    }
                }
                Button(
                    onClick = {
                        if (ultima) {
                            UsageLog.increment(context, UsageLog.QUIZZES)
                            terminado = true
                        } else {
                            indice += 1
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                ) { Text(if (ultima) "Ver mi resultado" else "Siguiente pregunta") }
            }
        }
    }
}

@Composable
private fun OpcionQuiz(
    texto: String,
    foto: Int?,
    marcada: Boolean,
    multiple: Boolean,
    respondida: Boolean,
    esCorrecta: Boolean,
    onClick: () -> Unit
) {
    val c = MaterialTheme.colorScheme
    val fondo = when {
        respondida && esCorrecta -> VerdeClaro
        respondida && marcada -> RojoClaro
        marcada -> c.primaryContainer
        else -> c.surface
    }
    val borde = when {
        respondida && esCorrecta -> VerdeOk
        respondida && marcada -> AlertaRoja
        marcada -> c.primary
        else -> c.outlineVariant
    }
    val grosor = if (marcada || (respondida && esCorrecta)) 2.dp else 1.dp

    Card(
        onClick = onClick,
        enabled = !respondida,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = fondo,
            disabledContainerColor = fondo,
            disabledContentColor = c.onSurface
        ),
        border = BorderStroke(grosor, borde)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (multiple) {
                Checkbox(checked = marcada, onCheckedChange = null)
            } else {
                RadioButton(selected = marcada, onClick = null)
            }
            if (foto != null) {
                Image(
                    painter = painterResource(foto),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
            Text(
                texto,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = c.onSurface
            )
            if (respondida && esCorrecta) {
                Icon(Icons.Filled.Check, contentDescription = "Respuesta correcta", tint = VerdeOk)
            } else if (respondida && marcada) {
                Icon(Icons.Filled.Close, contentDescription = "Respuesta incorrecta", tint = AlertaRoja)
            }
        }
    }
}

@Composable
private fun ResultadoQuiz(
    aciertos: Int,
    total: Int,
    onReintentar: () -> Unit,
    onIr: (Pantalla) -> Unit
) {
    val oscuro = LocalModoOscuro.current
    val porcentaje = if (total == 0) 0 else aciertos * 100 / total
    val mensaje = when {
        porcentaje >= 80 -> "¡Excelente! Ya sabes cómo prevenir el dengue y cuándo acudir al centro de salud."
        porcentaje >= 50 -> "¡Muy bien! Repasa los temas que fallaste y vuelve a intentarlo."
        else -> "Sigue aprendiendo. Revisa la información de la app y vuelve a intentarlo."
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(degradadoAzul(oscuro))
            .burbujas()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Filled.EmojiEvents,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(52.dp)
            )
            Text(
                "Tu resultado",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.85f)
            )
            Text(
                "$aciertos de $total",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                mensaje,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
    Button(
        onClick = onReintentar,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
    ) { Text("Intentar de nuevo") }
    OutlinedButton(
        onClick = { onIr(Pantalla.Prevencion) },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
    ) { Text("Repasar: prevención en el hogar") }
    OutlinedButton(
        onClick = { onIr(Pantalla.SignosAlarma) },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
    ) { Text("Repasar: signos de alarma") }
}
