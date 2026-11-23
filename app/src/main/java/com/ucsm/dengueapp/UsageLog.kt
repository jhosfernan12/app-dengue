package com.ucsm.dengueapp

import android.content.Context

object UsageLog {
    const val VIDEOS = "videos"
    const val QUIZZES = "quizzes"
    const val FOTOS = "fotos"
    const val REVISIONES = "revisiones"

    private const val PREFS = "uso_app"
    private const val VIDEOS_DISTINTOS = "videos_distintos"   // clave nueva: no choca con la anterior

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun increment(context: Context, key: String) {
        val p = prefs(context)
        p.edit().putInt(key, p.getInt(key, 0) + 1).apply()
    }

    /** Cuenta cada video una sola vez, sin importar cuántas veces lo abras. */
    fun registrarVideo(context: Context, videoId: String) {
        val p = prefs(context)
        val vistos = p.getStringSet(VIDEOS_DISTINTOS, emptySet()).orEmpty().toMutableSet()
        if (vistos.add(videoId)) {
            p.edit().putStringSet(VIDEOS_DISTINTOS, vistos).apply()
        }
    }

    fun get(context: Context, key: String): Int {
        val p = prefs(context)
        return if (key == VIDEOS) {
            p.getStringSet(VIDEOS_DISTINTOS, emptySet()).orEmpty().size
        } else {
            p.getInt(key, 0)
        }
    }
}