package com.example.vecizervi.data.utils

fun validarPassword(password: String): Boolean {
    val regex = Regex("^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\$%^&*(),.?\":{}|<>]).{8,}$")
    return regex.matches(password)
}