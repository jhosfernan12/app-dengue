package com.ucsm.dengueapp

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class Prediccion(
    val etiqueta: String,
    val confianza: Float,
    val todas: List<Pair<String, Float>>
)

class MosquitoClassifier(context: Context) : AutoCloseable {

    private val tamano = 224

    // Se quitan espacios y caracteres invisibles (BOM) para que "Aedes" coincida siempre
    val labels: List<String> = context.assets.open("labels.txt")
        .bufferedReader().readLines()
        .map { it.replace("\uFEFF", "").trim() }
        .filter { it.isNotEmpty() }

    private val interpreter: Interpreter

    init {
        val bytes = context.assets.open("mosquito_model.tflite").readBytes()
        val buffer = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder())
        buffer.put(bytes)
        buffer.rewind()
        interpreter = Interpreter(buffer)

        val nSalidas = interpreter.getOutputTensor(0).shape()[1]
        if (labels.size != nSalidas) {
            interpreter.close()
            throw IllegalStateException(
                "labels.txt tiene ${labels.size} clases ($labels) pero el modelo da $nSalidas. " +
                        "Los archivos de assets no son del mismo entrenamiento."
            )
        }
    }

    fun classify(bitmap: Bitmap): Prediccion {
        val reducida = Bitmap.createScaledBitmap(bitmap, tamano, tamano, true)
        val entrada = ByteBuffer.allocateDirect(1 * tamano * tamano * 3 * 4)
            .order(ByteOrder.nativeOrder())

        val pixeles = IntArray(tamano * tamano)
        reducida.getPixels(pixeles, 0, tamano, 0, 0, tamano, tamano)

        for (p in pixeles) {
            // RGB en escala 0-255 (igual que en el entrenamiento)
            entrada.putFloat(((p shr 16) and 0xFF).toFloat())
            entrada.putFloat(((p shr 8) and 0xFF).toFloat())
            entrada.putFloat((p and 0xFF).toFloat())
        }
        entrada.rewind()

        val salida = Array(1) { FloatArray(labels.size) }
        interpreter.run(entrada, salida)

        val ordenadas = labels.zip(salida[0].toList()).sortedByDescending { it.second }
        return Prediccion(ordenadas[0].first, ordenadas[0].second, ordenadas)
    }

    override fun close() {
        interpreter.close()
    }
}