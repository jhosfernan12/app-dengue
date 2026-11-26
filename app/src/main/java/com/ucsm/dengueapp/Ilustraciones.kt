package com.ucsm.dengueapp

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

// Ilustraciones propias del proyecto, dibujadas como vectores (res/drawable/img_*.xml).
// Se muestran sobre un fondo claro fijo para que se vean bien también en modo oscuro.

/** Franja con una ilustración centrada, para la parte de arriba de las pantallas informativas. */
@Composable
fun Ilustracion(@DrawableRes recurso: Int, descripcion: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 150.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(Color(0xFFE0F2F1), Color(0xFFE0F2FE)))),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(recurso),
            contentDescription = descripcion,
            modifier = Modifier.size(132.dp)
        )
    }
}
