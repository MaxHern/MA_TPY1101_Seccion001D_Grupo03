package com.example.vecizervi.data.utils

import java.util.Calendar

fun calcularEdad(fechaNacimiento: String): Int {
    val partes = fechaNacimiento.split("-")
    val year = partes[0].toInt()
    val month = partes[1].toInt()
    val day = partes[2].toInt()

    val hoy = Calendar.getInstance()
    val nacimiento = Calendar.getInstance()
    nacimiento.set(year, month - 1, day)
    var edad = hoy.get(Calendar.YEAR) - nacimiento.get(Calendar.YEAR)
    if (hoy.get(Calendar.DAY_OF_YEAR) < nacimiento.get(Calendar.DAY_OF_YEAR)) {
        edad--
    }

    return edad
}
