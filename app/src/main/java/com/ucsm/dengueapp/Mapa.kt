package com.ucsm.dengueapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ucsm.dengueapp.ui.theme.AlertaRoja
import com.ucsm.dengueapp.ui.theme.AmbarClaro
import com.ucsm.dengueapp.ui.theme.AmbarOscuro
import com.ucsm.dengueapp.ui.theme.Azul700
import com.ucsm.dengueapp.ui.theme.LocalModoOscuro
import com.ucsm.dengueapp.ui.theme.NaranjaFebril
import com.ucsm.dengueapp.ui.theme.VerdeClaro
import com.ucsm.dengueapp.ui.theme.VerdeOk
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.TilesOverlay

// ---------- Constantes ----------

const val RADIO_CERCANO_M = 1000.0      // "cerca de ti" = 1 km
const val VIGENCIA_DIAS = 30            // solo se muestran reportes de los últimos 30 días
private val MORADO = Color(0xFF6A1B9A)
private val PERMISOS_UBICACION = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

enum class EstadoUbicacion { SIN_PERMISO, BUSCANDO, LISTA, FALLO }

data class ReporteMapa(
    val id: String,
    val tipo: String,
    val lat: Double,
    val lon: Double,
    val validado: Boolean,
    val fechaMs: Long?
)

// ---------- Consentimiento y límite de envío ----------

object ConsentimientoMapa {
    private const val PREFS = "mapa"
    private const val CLAVE = "consentimiento_v3"
    private const val ULTIMO = "ultimo_envio"
    private const val ESPERA_MS = 10 * 60 * 1000L

    private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun aceptado(c: Context) = prefs(c).getBoolean(CLAVE, false)
    fun aceptar(c: Context) = prefs(c).edit().putBoolean(CLAVE, true).apply()

    fun puedeEnviar(c: Context): Boolean =
        System.currentTimeMillis() - prefs(c).getLong(ULTIMO, 0L) >= ESPERA_MS

    fun minutosRestantes(c: Context): Int {
        val falta = ESPERA_MS - (System.currentTimeMillis() - prefs(c).getLong(ULTIMO, 0L))
        return ((falta + 59_999) / 60_000).toInt().coerceAtLeast(1)
    }

    fun marcarEnvio(c: Context) = prefs(c).edit().putLong(ULTIMO, System.currentTimeMillis()).apply()

    /** Si borras un reporte por error, puedes volver a reportar sin esperar. */
    fun reiniciarEspera(c: Context) = prefs(c).edit().putLong(ULTIMO, 0L).apply()
}

/** Cuadrícula de ~500 m (0.005°). Lo que se envía nunca es la posición exacta. */
fun redondearZona(x: Double): Double = Math.round(x * 200.0) / 200.0

fun distanciaM(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val r = FloatArray(1)
    Location.distanceBetween(lat1, lon1, lat2, lon2, r)
    return r[0]
}

fun pluralMapa(n: Int, uno: String, varios: String) = "$n ${if (n == 1) uno else varios}"

fun tiempoRelativo(ms: Long?): String =
    if (ms == null) "hace un momento"
    else DateUtils.getRelativeTimeSpanString(ms, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString()

// ---------- Ubicación del teléfono (solo local) ----------

fun tienePermisoUbicacion(context: Context): Boolean =
    PERMISOS_UBICACION.any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

@Suppress("DEPRECATION")
@SuppressLint("MissingPermission")
fun obtenerUbicacion(context: Context, alRecibir: (Location?) -> Unit) {
    if (!tienePermisoUbicacion(context)) {
        alRecibir(null)
        return
    }
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val fino = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val candidatos =
        if (fino) listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        else listOf(LocationManager.NETWORK_PROVIDER)
    val proveedores = candidatos.filter { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }
    val ultima = proveedores
        .mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
        .maxByOrNull { it.time }
    val proveedor = proveedores.firstOrNull()
    if (proveedor == null) {
        alRecibir(ultima)
        return
    }

    val manejador = Handler(Looper.getMainLooper())
    var terminado = false
    lateinit var oyente: LocationListener

    val terminar: (Location?) -> Unit = { loc ->
        if (!terminado) {
            terminado = true
            manejador.removeCallbacksAndMessages(null)
            runCatching { lm.removeUpdates(oyente) }
            alRecibir(loc ?: ultima)
        }
    }

    oyente = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            terminar(location)
        }

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    try {
        lm.requestSingleUpdate(proveedor, oyente, Looper.getMainLooper())
        manejador.postDelayed({ terminar(null) }, 15_000L)   // tiempo máximo de espera
    } catch (e: Exception) {
        terminar(null)
    }
}

fun abrirAjustesApp(context: Context) {
    context.startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
    )
}

fun abrirAjustesGps(context: Context) {
    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
}

// ---------- Firebase ----------

private var inicioSesion: Task<AuthResult>? = null

/** Garantiza una sola sesión anónima (si hubiera dos, el dueño de los reportes cambiaría). */
fun conSesion(onListo: (String) -> Unit, onError: (String) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    auth.currentUser?.let {
        onListo(it.uid)
        return
    }
    val tarea = inicioSesion ?: auth.signInAnonymously().also { inicioSesion = it }
    tarea
        .addOnSuccessListener {
            inicioSesion = null
            val uid = auth.currentUser?.uid
            if (uid != null) onListo(uid) else onError("no se pudo iniciar sesión")
        }
        .addOnFailureListener {
            inicioSesion = null
            onError(it.message ?: "no se pudo iniciar sesión")
        }
}

fun cargarReportes(onOk: (List<ReporteMapa>) -> Unit, onError: (String) -> Unit) {
    val limite = System.currentTimeMillis() - VIGENCIA_DIAS * 24L * 60 * 60 * 1000
    conSesion(
        onListo = {
            FirebaseFirestore.getInstance().collection("reportes")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .limit(500)
                .get()
                .addOnSuccessListener { snap ->
                    val lista = snap.documents.mapNotNull { d ->
                        val tipo = d.getString("tipo") ?: return@mapNotNull null
                        val lat = d.getDouble("lat") ?: return@mapNotNull null
                        val lon = d.getDouble("lon") ?: return@mapNotNull null
                        val fecha = d.getTimestamp("fecha")?.toDate()?.time
                        if (fecha != null && fecha < limite) return@mapNotNull null
                        ReporteMapa(d.id, tipo, lat, lon, d.getBoolean("validado") == true, fecha)
                    }
                    onOk(lista)
                }
                .addOnFailureListener { onError(it.message ?: "error desconocido") }
        },
        onError = onError
    )
}

/** Ids de los reportes que envió este usuario (la regla solo deja leer los propios). */
fun cargarMisReportes(onOk: (Set<String>) -> Unit, onError: (String) -> Unit) {
    conSesion(
        onListo = { uid ->
            FirebaseFirestore.getInstance().collection("propietarios")
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener { snap -> onOk(snap.documents.map { it.id }.toSet()) }
                .addOnFailureListener { onError(it.message ?: "error desconocido") }
        },
        onError = onError
    )
}

fun enviarReporte(
    tipo: String,
    lat: Double,
    lon: Double,
    onOk: () -> Unit,
    onError: (String) -> Unit
) {
    conSesion(
        onListo = { uid ->
            val db = FirebaseFirestore.getInstance()
            val ref = db.collection("reportes").document()
            val datos = hashMapOf<String, Any>(
                "tipo" to tipo,
                "lat" to redondearZona(lat),
                "lon" to redondearZona(lon),
                "validado" to false,
                "fecha" to FieldValue.serverTimestamp()
            )
            val batch = db.batch()
            batch.set(ref, datos)
            batch.set(db.collection("propietarios").document(ref.id), hashMapOf<String, Any>("uid" to uid))
            batch.commit()
                .addOnSuccessListener { onOk() }
                .addOnFailureListener { onError(it.message ?: "error desconocido") }
        },
        onError = onError
    )
}

fun eliminarReporte(id: String, onOk: () -> Unit, onError: (String) -> Unit) {
    conSesion(
        onListo = {
            val db = FirebaseFirestore.getInstance()
            val batch = db.batch()
            batch.delete(db.collection("reportes").document(id))
            batch.delete(db.collection("propietarios").document(id))
            batch.commit()
                .addOnSuccessListener { onOk() }
                .addOnFailureListener { onError(it.message ?: "error desconocido") }
        },
        onError = onError
    )
}

// ---------- Dibujo del mapa ----------

fun iconoCirculo(context: Context, color: Int, numero: Int?): Drawable {
    val d = context.resources.displayMetrics.density
    val tam = (42 * d).toInt()
    val bmp = Bitmap.createBitmap(tam, tam, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val p = Paint(Paint.ANTI_ALIAS_FLAG)
    p.color = android.graphics.Color.WHITE
    canvas.drawCircle(tam / 2f, tam / 2f, tam / 2f, p)
    p.color = color
    canvas.drawCircle(tam / 2f, tam / 2f, tam / 2f - 3 * d, p)
    if (numero != null) {
        p.color = android.graphics.Color.WHITE
        p.textAlign = Paint.Align.CENTER
        p.textSize = 16 * d
        p.isFakeBoldText = true
        val texto = if (numero > 99) "99+" else numero.toString()
        canvas.drawText(texto, tam / 2f, tam / 2f - (p.descent() + p.ascent()) / 2f, p)
    } else {
        p.color = android.graphics.Color.WHITE
        canvas.drawCircle(tam / 2f, tam / 2f, 4 * d, p)
    }
    return BitmapDrawable(context.resources, bmp)
}

fun dibujarMapa(
    context: Context,
    mapa: MapView,
    reportes: List<ReporteMapa>,
    ubicacion: GeoPoint?,
    misIds: Set<String>
) {
    mapa.overlays.clear()

    if (ubicacion != null) {
        val circulo = Polygon()
        circulo.setPoints(Polygon.pointsAsCircle(ubicacion, RADIO_CERCANO_M))
        circulo.fillPaint.color = android.graphics.Color.argb(40, 15, 118, 110)
        circulo.outlinePaint.color = android.graphics.Color.argb(170, 15, 118, 110)
        circulo.outlinePaint.strokeWidth = 4f
        mapa.overlays.add(circulo)
    }

    reportes.groupBy { "${it.lat}|${it.lon}" }.values.forEach { grupo ->
        val primero = grupo.first()
        val criaderos = grupo.count { it.tipo == "criadero" }
        val avistamientos = grupo.size - criaderos
        val validados = grupo.count { it.validado }
        val mios = grupo.count { it.id in misIds }
        val color = when {
            criaderos > 0 && avistamientos > 0 -> MORADO.toArgb()
            criaderos > 0 -> NaranjaFebril.toArgb()
            else -> AlertaRoja.toArgb()
        }
        val marcador = Marker(mapa)
        marcador.position = GeoPoint(primero.lat, primero.lon)
        marcador.icon = iconoCirculo(context, color, grupo.size)
        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        marcador.title = pluralMapa(grupo.size, "reporte en esta zona", "reportes en esta zona")
        marcador.snippet = pluralMapa(criaderos, "criadero", "criaderos") + ", " +
                pluralMapa(avistamientos, "mosquito", "mosquitos") + ". " +
                pluralMapa(validados, "validado", "validados") + "." +
                (if (mios > 0) " Incluye ${pluralMapa(mios, "reporte tuyo", "reportes tuyos")}." else "") +
                " Aviso de la comunidad."
        mapa.overlays.add(marcador)
    }

    if (ubicacion != null) {
        val yo = Marker(mapa)
        yo.position = ubicacion
        yo.icon = iconoCirculo(context, Azul700.toArgb(), null)
        yo.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        yo.title = "Tú estás aquí"
        mapa.overlays.add(yo)
    }
    mapa.invalidate()
}

// ---------- Pantalla principal del mapa ----------

@Composable
fun PantallaMapa(onVolver: () -> Unit, onChecklist: () -> Unit = {}) {
    val context = LocalContext.current
    var aceptado by remember { mutableStateOf(ConsentimientoMapa.aceptado(context)) }
    if (!aceptado) {
        PantallaConsentimiento(onVolver) {
            ConsentimientoMapa.aceptar(context)
            aceptado = true
        }
    } else {
        MapaReportes(onVolver, onChecklist)
    }
}

@Composable
fun PantallaConsentimiento(onVolver: () -> Unit, onAceptar: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Pagina("Mapa comunitario", onVolver) {
        Cabecera(
            "Mapa comunitario",
            "Mira si hay reportes de criaderos o mosquitos cerca de ti y avisa a tu comunidad."
        )
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = c.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Antes de continuar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Vineta("Tu ubicación se usa solo dentro de tu teléfono, para abrir el mapa donde estás y buscar reportes cercanos. No se envía ni se guarda.")
                Vineta("Si reportas un criadero o un mosquito, se envía una zona aproximada (cuadrícula de unos 500 m) marcada por el pin. No se envía tu nombre, tu teléfono ni tu dirección.")
                Vineta("Para que puedas eliminar tus propios reportes, cada uno queda asociado a un identificador anónimo de tu instalación de la app. Ese identificador no es visible para otros usuarios. Si borras los datos de la app, ya no podrás eliminar los reportes anteriores.")
                Vineta("Los reportes son avisos de la comunidad y no están confirmados, salvo los marcados como validados.")
                Vineta("Este mapa no es un servicio de emergencia. Si tienes fiebre y signos de alarma, acude a un establecimiento de salud.")
            }
        }
        Button(onClick = onAceptar, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
            Text("Acepto y continuar")
        }
        OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
            Text("No, volver")
        }
    }
}

@Composable
fun MapaReportes(onVolver: () -> Unit, onChecklist: () -> Unit) {
    val context = LocalContext.current
    val c = MaterialTheme.colorScheme
    val oscuro = LocalModoOscuro.current

    val mapa = remember {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            minZoomLevel = 4.0
            maxZoomLevel = 19.0
            controller.setZoom(13.0)
            controller.setCenter(GeoPoint(-16.409, -71.537))   // Arequipa, mientras llega el GPS
        }
    }
    DisposableEffect(mapa) {
        mapa.onResume()
        onDispose {
            mapa.onPause()
            mapa.onDetach()
        }
    }
    LaunchedEffect(oscuro) {
        mapa.overlayManager.tilesOverlay.setColorFilter(
            if (oscuro) TilesOverlay.INVERT_COLORS else null
        )
        mapa.invalidate()
    }

    var estado by remember { mutableStateOf(EstadoUbicacion.BUSCANDO) }
    var ubicacion by remember { mutableStateOf<GeoPoint?>(null) }
    var centrado by remember { mutableStateOf(false) }
    var reportes by remember { mutableStateOf<List<ReporteMapa>>(emptyList()) }
    var misIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var cargando by remember { mutableStateOf(true) }
    var errorReportes by remember { mutableStateOf<String?>(null) }
    var enviando by remember { mutableStateOf(false) }
    var confirmar by remember { mutableStateOf<String?>(null) }
    var mostrarMios by remember { mutableStateOf(false) }
    var aEliminar by remember { mutableStateOf<ReporteMapa?>(null) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }

    val buscar: () -> Unit = {
        estado = EstadoUbicacion.BUSCANDO
        obtenerUbicacion(context) { loc ->
            if (loc != null) {
                ubicacion = GeoPoint(loc.latitude, loc.longitude)
                estado = EstadoUbicacion.LISTA
            } else {
                estado = EstadoUbicacion.FALLO
            }
        }
    }

    val lanzador = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        if (permisos.values.any { it }) buscar() else estado = EstadoUbicacion.SIN_PERMISO
    }

    val activarUbicacion: () -> Unit = {
        if (tienePermisoUbicacion(context)) buscar() else lanzador.launch(PERMISOS_UBICACION)
    }

    val recargar: () -> Unit = {
        cargando = true
        errorReportes = null
        cargarReportes(
            onOk = { lista ->
                reportes = lista
                cargando = false
            },
            onError = { msg ->
                errorReportes = msg
                cargando = false
            }
        )
        cargarMisReportes(
            onOk = { ids -> misIds = ids },
            onError = { }
        )
    }

    val centrarEnMi: () -> Unit = {
        val u = ubicacion
        if (u != null) {
            mapa.controller.setZoom(16.0)
            mapa.controller.animateTo(u)
        } else {
            activarUbicacion()
        }
    }

    val reportar: (String) -> Unit = { tipo ->
        if (!ConsentimientoMapa.puedeEnviar(context)) {
            mensaje = "Espera ${ConsentimientoMapa.minutosRestantes(context)} min para enviar otro reporte."
        } else {
            val centro = mapa.mapCenter
            enviando = true
            enviarReporte(
                tipo, centro.latitude, centro.longitude,
                onOk = {
                    ConsentimientoMapa.marcarEnvio(context)
                    enviando = false
                    mensaje = "¡Gracias! Reporte enviado."
                    recargar()
                },
                onError = {
                    enviando = false
                    mensaje = "No se pudo enviar: $it"
                }
            )
        }
    }

    val eliminar: (ReporteMapa) -> Unit = { r ->
        eliminarReporte(
            r.id,
            onOk = {
                ConsentimientoMapa.reiniciarEspera(context)
                reportes = reportes.filter { it.id != r.id }
                misIds = misIds - r.id
                mensaje = "Reporte eliminado."
            },
            onError = { mensaje = "No se pudo eliminar: $it" }
        )
    }

    LaunchedEffect(Unit) {
        recargar()
        if (tienePermisoUbicacion(context)) buscar() else lanzador.launch(PERMISOS_UBICACION)
    }

    LaunchedEffect(ubicacion) {
        val u = ubicacion ?: return@LaunchedEffect
        if (!centrado) {
            mapa.controller.setZoom(15.0)
            mapa.controller.setCenter(u)
            centrado = true
        }
    }

    LaunchedEffect(reportes, ubicacion, misIds) {
        dibujarMapa(context, mapa, reportes, ubicacion, misIds)
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbar.showSnackbar(it)
            mensaje = null
        }
    }

    val cercanos = remember(reportes, ubicacion) {
        ubicacion?.let { u ->
            reportes.filter { distanciaM(u.latitude, u.longitude, it.lat, it.lon) <= RADIO_CERCANO_M }
        }
    }
    val misReportes = remember(reportes, misIds) { reportes.filter { it.id in misIds } }

    // ----- Diálogos -----

    confirmar?.let { tipo ->
        AlertDialog(
            onDismissRequest = { confirmar = null },
            title = { Text(if (tipo == "criadero") "¿Reportar criadero?" else "¿Reportar mosquito?") },
            text = {
                Text(
                    "Se enviará la zona marcada por el pin rojo, de forma aproximada (unos 500 m). " +
                            "No se envía tu nombre, tu teléfono ni tu ubicación exacta. " +
                            "Si te equivocas, podrás eliminarlo desde \"Mis reportes\"."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmar = null
                    reportar(tipo)
                }) { Text("Enviar") }
            },
            dismissButton = { TextButton(onClick = { confirmar = null }) { Text("Cancelar") } }
        )
    }

    if (mostrarMios) {
        AlertDialog(
            onDismissRequest = { mostrarMios = false },
            title = { Text("Mis reportes") },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Solo tú puedes eliminar los reportes que enviaste desde este teléfono. " +
                                "Los reportes ya validados no se pueden eliminar desde la app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.onSurfaceVariant
                    )
                    if (misReportes.isEmpty()) {
                        Text("No tienes reportes recientes.")
                    }
                    misReportes.forEach { r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    if (r.tipo == "criadero") "Criadero" else "Mosquito",
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    tiempoRelativo(r.fechaMs) + if (r.validado) " · validado" else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = c.onSurfaceVariant
                                )
                            }
                            if (!r.validado) {
                                TextButton(
                                    onClick = { aEliminar = r },
                                    colors = ButtonDefaults.textButtonColors(contentColor = c.error)
                                ) { Text("Eliminar") }
                            }
                        }
                        HorizontalDivider()
                    }
                }
            },
            confirmButton = { TextButton(onClick = { mostrarMios = false }) { Text("Cerrar") } }
        )
    }

    aEliminar?.let { r ->
        AlertDialog(
            onDismissRequest = { aEliminar = null },
            title = { Text("¿Eliminar este reporte?") },
            text = {
                Text(
                    "Se borrará tu reporte de " +
                            (if (r.tipo == "criadero") "criadero" else "mosquito") +
                            ". Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        aEliminar = null
                        eliminar(r)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = c.error)
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { aEliminar = null }) { Text("Cancelar") } }
        )
    }

    // ----- Pantalla -----

    Scaffold(
        topBar = { BarraSuperior("Mapa comunitario", onVolver) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            AndroidView(factory = { mapa }, modifier = Modifier.fillMaxSize())

            // Pin fijo en el centro: marca la zona que se reporta
            Icon(
                Icons.Filled.Place,
                contentDescription = "Zona del reporte",
                tint = AlertaRoja,
                modifier = Modifier.align(Alignment.Center).offset(y = (-20).dp).size(40.dp)
            )

            TarjetaCercanos(
                estado = estado,
                cargando = cargando,
                error = errorReportes,
                cercanos = cercanos,
                onActivar = activarUbicacion,
                onAjustesApp = { abrirAjustesApp(context) },
                onAjustesGps = { abrirAjustesGps(context) },
                onRevisarCasa = onChecklist,
                modifier = Modifier.align(Alignment.TopCenter).padding(12.dp)
            )

            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
                ) {
                    SmallFloatingActionButton(
                        onClick = recargar,
                        containerColor = c.surface,
                        contentColor = c.primary
                    ) { Icon(Icons.Filled.Refresh, contentDescription = "Actualizar mapa") }
                    SmallFloatingActionButton(
                        onClick = centrarEnMi,
                        containerColor = c.surface,
                        contentColor = c.primary
                    ) { Icon(Icons.Filled.MyLocation, contentDescription = "Ir a mi ubicación") }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = c.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Mueve el mapa: el pin rojo marca la zona que vas a reportar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { confirmar = "criadero" },
                                enabled = !enviando,
                                modifier = Modifier.weight(1f).fillMaxHeight().heightIn(min = 52.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NaranjaFebril)
                            ) { Text("Reportar criadero", textAlign = TextAlign.Center) }
                            Button(
                                onClick = { confirmar = "avistamiento" },
                                enabled = !enviando,
                                modifier = Modifier.weight(1f).fillMaxHeight().heightIn(min = 52.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AlertaRoja)
                            ) { Text("Reportar mosquito", textAlign = TextAlign.Center) }
                        }
                        if (misReportes.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { mostrarMios = true },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp)
                            ) { Text("Mis reportes (${misReportes.size}): ver o eliminar") }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PuntoLeyenda(NaranjaFebril, "Criadero")
                            PuntoLeyenda(AlertaRoja, "Mosquito")
                            PuntoLeyenda(MORADO, "Ambos")
                            PuntoLeyenda(Azul700, "Tú")
                        }
                        Text(
                            "Mapa © OpenStreetMap contributors. Avisos de la comunidad, no confirmados salvo los validados. Últimos $VIGENCIA_DIAS días.",
                            style = MaterialTheme.typography.labelSmall,
                            color = c.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PuntoLeyenda(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Text(texto, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun EncabezadoMapa(icono: ImageVector, titulo: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
        Text(
            titulo,
            modifier = Modifier.weight(1f),
            color = color,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun TarjetaCercanos(
    estado: EstadoUbicacion,
    cargando: Boolean,
    error: String?,
    cercanos: List<ReporteMapa>?,
    onActivar: () -> Unit,
    onAjustesApp: () -> Unit,
    onAjustesGps: () -> Unit,
    onRevisarCasa: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme
    val n = cercanos?.size ?: 0
    val hayResumen = estado == EstadoUbicacion.LISTA && !cargando && error == null && cercanos != null

    val fondoObj = when {
        !hayResumen -> c.surface
        n == 0 -> VerdeClaro
        n < 3 -> AmbarClaro
        else -> AlertaRoja
    }
    val textoObj = when {
        !hayResumen -> c.onSurface
        n == 0 -> VerdeOk
        n < 3 -> AmbarOscuro
        else -> Color.White
    }
    val fondo by animateColorAsState(fondoObj, label = "fondoCercanos")
    val texto by animateColorAsState(textoObj, label = "textoCercanos")

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = fondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            when (estado) {
                EstadoUbicacion.SIN_PERMISO -> {
                    EncabezadoMapa(Icons.Filled.Place, "Activa tu ubicación", texto)
                    Text(
                        "Así el mapa se abre donde estás y te avisa si hay reportes cerca. Tu ubicación no sale de tu teléfono.",
                        color = texto, style = MaterialTheme.typography.bodySmall
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onActivar) { Text("Activar") }
                        TextButton(onClick = onAjustesApp) { Text("Ajustes de la app") }
                    }
                }

                EstadoUbicacion.BUSCANDO -> {
                    EncabezadoMapa(Icons.Filled.Place, "Buscando tu ubicación…", texto)
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }

                EstadoUbicacion.FALLO -> {
                    EncabezadoMapa(Icons.Filled.Warning, "No pudimos ubicarte", texto)
                    Text(
                        "Revisa que el GPS esté activado y vuelve a intentar.",
                        color = texto, style = MaterialTheme.typography.bodySmall
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onActivar) { Text("Reintentar") }
                        TextButton(onClick = onAjustesGps) { Text("Ajustes de ubicación") }
                    }
                }

                EstadoUbicacion.LISTA -> {
                    when {
                        cargando -> {
                            EncabezadoMapa(Icons.Filled.Place, "Buscando reportes cercanos…", texto)
                            LinearProgressIndicator(Modifier.fillMaxWidth())
                        }

                        error != null -> {
                            EncabezadoMapa(Icons.Filled.Warning, "No se pudieron cargar los reportes", texto)
                            Text(error, color = texto, style = MaterialTheme.typography.bodySmall)
                        }

                        cercanos != null && n == 0 -> {
                            EncabezadoMapa(Icons.Filled.Check, "Sin reportes cerca de ti", texto)
                            Text(
                                "No hay avisos a menos de 1 km en los últimos $VIGENCIA_DIAS días. " +
                                        "Eso no significa que no haya mosquitos: sigue revisando tu casa.",
                                color = texto, style = MaterialTheme.typography.bodySmall
                            )
                        }

                        cercanos != null -> {
                            val criaderos = cercanos.count { it.tipo == "criadero" }
                            val avistamientos = n - criaderos
                            val validados = cercanos.count { it.validado }
                            EncabezadoMapa(
                                Icons.Filled.Warning,
                                pluralMapa(n, "aviso cerca de ti", "avisos cerca de ti"),
                                texto
                            )
                            Text(
                                pluralMapa(criaderos, "criadero", "criaderos") + " y " +
                                        pluralMapa(avistamientos, "mosquito", "mosquitos") +
                                        " a menos de 1 km (" + pluralMapa(validados, "validado", "validados") +
                                        "). Son avisos de la comunidad.",
                                color = texto, style = MaterialTheme.typography.bodySmall
                            )
                            TextButton(
                                onClick = onRevisarCasa,
                                colors = ButtonDefaults.textButtonColors(contentColor = texto)
                            ) { Text("Revisar mi casa", fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }
    }
}