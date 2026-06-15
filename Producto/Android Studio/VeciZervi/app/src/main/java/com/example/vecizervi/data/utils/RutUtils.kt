package com.example.vecizervi.data.utils

fun validarRut(rut: String): Boolean {
    return rut.matches(Regex("^[0-9]+-[0-9kK]$"))
}

