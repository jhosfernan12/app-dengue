package com.ucsm.dengueapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ucsm.dengueapp.ui.theme.VerdeClaro
import com.ucsm.dengueapp.ui.theme.VerdeOk

// ---------- Encuesta de satisfacción ----------
//
// Es anónima: no se guarda nombre, correo, teléfono ni identificador del usuario.
// Cada respuesta se guarda como un documento nuevo en la colección "encuestas" de Firestore
// (ver las reglas en el README). Los resultados se leen desde la consola de Firebase.

private val PREGUNTAS = listOf(
    "La app es fácil de usar.",
    "La información sobre el dengue es clara y fácil de entender.",
    "El diseño y las ilustraciones son agradables.",
    "La voz de lectura se entiende bien y suena natural.",
    "La función de identificar mosquito (experimental) me parece útil.",
    "El mapa comunitario me parece útil."
)

private val OPCIONES_RECOMIENDA = listOf(
    "si" to "Sí",
    "tal_vez" to "Tal vez",
    "no" to "No"
)

private const val MAX_COMENTARIO = 300

fun enviarEncuesta(
    notas: List<Int>,
    recomienda: String,
    comentario: String,
    onOk: () -> Unit,
    onError: (String) -> Unit
) {
    conSesion(
        onListo = {
            val datos = hashMapOf<String, Any>(
                "recomienda" to recomienda,
                "fecha" to FieldValue.serverTimestamp(),
                "version" to "1.1"
            )
            notas.forEachIndexed { i, nota -> datos["p${i + 1}"] = nota }
            val texto = comentario.trim().take(MAX_COMENTARIO)
            if (texto.isNotEmpty()) datos["comentario"] = texto

            FirebaseFirestore.getInstance().collection("encuestas")
                .add(datos)
                .addOnSuccessListener { onOk() }
                .addOnFailureListener { onError(it.message ?: "error desconocido") }
        },
        onError = onError
    )
}

@Composable
fun PantallaEncuesta(onVolver: () -> Unit) {
    val context = LocalContext.current
    val c = MaterialTheme.colorScheme

    var notas by rememberSaveable { mutableStateOf(List(PREGUNTAS.size) { 0 }) }
    var recomienda by rememberSaveable { mutableStateOf("") }
    var comentario by rememberSaveable { mutableStateOf("") }
    var enviado by rememberSaveable { mutableStateOf(false) }
    var enviando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val completa = notas.all { it in 1..5 } && recomienda.isNotEmpty()

    Pagina("Tu opinión", onVolver) {
        if (enviado) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = VerdeClaro)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = VerdeOk)
                        Text(
                            "¡Gracias por tu opinión!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = VerdeOk
                        )
                    }
                    Text(
                        "Tu respuesta se envió de forma anónima y nos ayuda a mejorar $NOMBRE_APP.",
                        color = VerdeOk
                    )
                }
            }
            Button(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            ) { Text("Volver al inicio") }
            TextButton(
                onClick = {
                    notas = List(PREGUNTAS.size) { 0 }
                    recomienda = ""
                    comentario = ""
                    error = null
                    enviado = false
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Responder de nuevo (otra persona)") }
            return@Pagina
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = c.primaryContainer)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Encuesta de satisfacción",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = c.onPrimaryContainer
                )
                Text(
                    "Es anónima: no pedimos nombre, correo ni teléfono. Toma menos de 2 minutos. " +
                            "Elige un número del 1 al 5, donde 1 es \"nada de acuerdo\" y 5 es \"totalmente de acuerdo\".",
                    color = c.onPrimaryContainer
                )
            }
        }

        PREGUNTAS.forEachIndexed { i, pregunta ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = c.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "${i + 1}. $pregunta",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (valor in 1..5) {
                            val alElegir = {
                                notas = notas.toMutableList().also { it[i] = valor }
                            }
                            if (notas[i] == valor) {
                                Button(
                                    onClick = alElegir,
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.weight(1f)
                                ) { Text("$valor", maxLines = 1) }
                            } else {
                                OutlinedButton(
                                    onClick = alElegir,
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.weight(1f)
                                ) { Text("$valor", maxLines = 1) }
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = c.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "${PREGUNTAS.size + 1}. ¿Recomendarías esta app a otras personas?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OPCIONES_RECOMIENDA.forEach { (clave, etiqueta) ->
                        if (recomienda == clave) {
                            Button(
                                onClick = { recomienda = clave },
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) { Text(etiqueta, maxLines = 1) }
                        } else {
                            OutlinedButton(
                                onClick = { recomienda = clave },
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) { Text(etiqueta, maxLines = 1) }
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = comentario,
            onValueChange = { comentario = it.take(MAX_COMENTARIO) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Comentario o sugerencia (opcional)") },
            supportingText = { Text("${comentario.length}/$MAX_COMENTARIO. No escribas datos personales.") },
            minLines = 3,
            maxLines = 6
        )

        error?.let { Text(it, color = c.error) }

        if (!completa) {
            Text(
                "Responde todas las preguntas para poder enviar.",
                style = MaterialTheme.typography.bodySmall,
                color = c.onSurfaceVariant
            )
        }

        Button(
            onClick = {
                error = null
                if (!hayInternet(context)) {
                    error = "Necesitas internet para enviar la encuesta. Conéctate e inténtalo de nuevo."
                } else {
                    enviando = true
                    enviarEncuesta(
                        notas = notas,
                        recomienda = recomienda,
                        comentario = comentario,
                        onOk = {
                            enviando = false
                            enviado = true
                        },
                        onError = {
                            enviando = false
                            error = "No se pudo enviar la encuesta ($it). Inténtalo de nuevo."
                        }
                    )
                }
            },
            enabled = completa && !enviando,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
        ) { Text(if (enviando) "Enviando…" else "Enviar encuesta") }
    }
}
