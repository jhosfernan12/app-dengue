package com.ucsm.dengueapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/** true si el teléfono tiene alguna red con salida a internet. */
fun hayInternet(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val red = cm.activeNetwork ?: return false
    val capacidades = cm.getNetworkCapabilities(red) ?: return false
    return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
