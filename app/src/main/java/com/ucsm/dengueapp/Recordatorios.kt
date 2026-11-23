package com.ucsm.dengueapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.IsoFields
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit

const val CANAL_RECORDATORIOS = "recordatorios_dengue"

// ---------- Datos de la rutina (se guardan en el teléfono) ----------

object Rutina {
    private const val PREFS = "rutina"
    private const val TRABAJO = "recordatorio_semanal"

    private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // Clave de semana ISO, por ejemplo 202646
    private fun clave(f: LocalDate): Int =
        f.get(IsoFields.WEEK_BASED_YEAR) * 100 + f.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)

    fun activado(c: Context): Boolean = prefs(c).getBoolean("activado", false)

    fun dia(c: Context): Int = prefs(c).getInt("dia", DayOfWeek.SATURDAY.value)

    /** Se llama cuando el usuario guarda una revisión de la casa. */
    fun registrarRevision(c: Context) {
        val hoy = LocalDate.now()
        val actual = clave(hoy)
        val ultima = prefs(c).getInt("ultima_semana", 0)
        if (ultima == actual) return                      // ya contó esta semana
        val racha = prefs(c).getInt("racha", 0)
        val nueva = if (ultima == clave(hoy.minusWeeks(1))) racha + 1 else 1
        prefs(c).edit().putInt("ultima_semana", actual).putInt("racha", nueva).apply()
    }

    fun revisadoEstaSemana(c: Context): Boolean =
        prefs(c).getInt("ultima_semana", 0) == clave(LocalDate.now())

    /** Semanas seguidas. Vale 0 si se saltó una semana. */
    fun racha(c: Context): Int {
        val hoy = LocalDate.now()
        val ultima = prefs(c).getInt("ultima_semana", 0)
        return if (ultima == clave(hoy) || ultima == clave(hoy.minusWeeks(1))) {
            prefs(c).getInt("racha", 0)
        } else 0
    }

    fun activar(c: Context, dia: Int) {
        prefs(c).edit().putBoolean("activado", true).putInt("dia", dia).apply()

        // Cuánto falta para el próximo día elegido a las 9:00 (hora del teléfono)
        val ahora = LocalDateTime.now()
        var objetivo = ahora.with(TemporalAdjusters.nextOrSame(DayOfWeek.of(dia)))
            .withHour(9).withMinute(0).withSecond(0).withNano(0)
        if (!objetivo.isAfter(ahora)) objetivo = objetivo.plusWeeks(1)
        val retraso = Duration.between(ahora, objetivo).toMillis()

        val pedido = PeriodicWorkRequestBuilder<RecordatorioWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(retraso, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(c)
            .enqueueUniquePeriodicWork(TRABAJO, ExistingPeriodicWorkPolicy.UPDATE, pedido)
    }

    fun desactivar(c: Context) {
        prefs(c).edit().putBoolean("activado", false).apply()
        WorkManager.getInstance(c).cancelUniqueWork(TRABAJO)
    }
}

// ---------- Tarea que lanza la notificación ----------

class RecordatorioWorker(contexto: Context, params: WorkerParameters) : Worker(contexto, params) {

    override fun doWork(): Result {
        val c = applicationContext

        // Sin permiso de notificaciones (Android 13 o superior) no se hace nada
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(c, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return Result.success()

        // Si ya hizo su revisión esta semana, no se molesta
        if (Rutina.revisadoEstaSemana(c)) return Result.success()

        val gestor = c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        gestor.createNotificationChannel(
            NotificationChannel(
                CANAL_RECORDATORIOS,
                "Recordatorios de prevención",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )

        val abrir = Intent(c, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendiente = PendingIntent.getActivity(
            c, 0, abrir, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificacion = NotificationCompat.Builder(c, CANAL_RECORDATORIOS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Revisa tu casa esta semana")
            .setContentText("Lava, escobilla y tapa tus recipientes con agua. ¡Mantén tu racha!")
            .setContentIntent(pendiente)
            .setAutoCancel(true)
            .build()
        gestor.notify(1001, notificacion)
        return Result.success()
    }
}

// ---------- Pantalla ----------

@Composable
fun PantallaRutina(onVolver: () -> Unit, onRevisar: () -> Unit) {
    val context = LocalContext.current
    var activado by remember { mutableStateOf(Rutina.activado(context)) }
    var dia by remember { mutableIntStateOf(Rutina.dia(context)) }
    val racha = Rutina.racha(context)
    val hecho = Rutina.revisadoEstaSemana(context)

    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) {
            Rutina.activar(context, dia)
            activado = true
        } else {
            Toast.makeText(
                context,
                "Sin permiso de notificaciones no se pueden enviar recordatorios",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val dias = listOf("Lu", "Ma", "Mi", "Ju", "Vi", "Sá", "Do")

    Pagina("Mi rutina semanal", onVolver) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Racha: $racha ${if (racha == 1) "semana" else "semanas"} seguidas",
                    style = MaterialTheme.typography.titleMedium)
                Text(
                    if (hecho) "✔ Esta semana ya revisaste tu casa"
                    else "Esta semana todavía no revisas tu casa",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Button(
            onClick = onRevisar,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) { Text("Hacer mi revisión ahora") }

        HorizontalDivider()

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Recordatorio semanal", modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall)
            Switch(
                checked = activado,
                onCheckedChange = { quiere ->
                    if (!quiere) {
                        Rutina.desactivar(context)
                        activado = false
                    } else if (Build.VERSION.SDK_INT >= 33 &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        Rutina.activar(context, dia)
                        activado = true
                    }
                }
            )
        }

        Text("¿Qué día quieres que te avisemos?", style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            dias.forEachIndexed { i, etiqueta ->
                val valor = i + 1
                val alElegir = {
                    dia = valor
                    if (activado) Rutina.activar(context, valor)
                }
                if (dia == valor) {
                    Button(
                        onClick = alElegir,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.weight(1f)
                    ) { Text(etiqueta, maxLines = 1) }
                } else {
                    OutlinedButton(
                        onClick = alElegir,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.weight(1f)
                    ) { Text(etiqueta, maxLines = 1) }
                }
            }
        }
        Text(
            "El aviso llega alrededor de las 9:00. Android puede adelantarlo o retrasarlo unos minutos. " +
                    "Si ya hiciste tu revisión esa semana, no te molestamos.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}