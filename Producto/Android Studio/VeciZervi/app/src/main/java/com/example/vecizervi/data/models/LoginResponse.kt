package com.example.vecizervi.data.models

import com.google.gson.annotations.SerializedName

// S04: respuesta del login ahora incluye el token JWT
data class LoginResponse(
    @SerializedName("usuario") val usuario: Usuario,
    @SerializedName("token")   val token: String
)
