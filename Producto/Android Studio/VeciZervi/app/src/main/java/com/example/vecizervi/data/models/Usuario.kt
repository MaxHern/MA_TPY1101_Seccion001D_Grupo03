package com.example.vecizervi.data.models

import com.google.gson.annotations.SerializedName

data class Usuario(
    @SerializedName("idUsuario") val idUsuario: Int? = null,
    @SerializedName("rut") val rut: String = "",
    @SerializedName("nombres") val nombres: String = "",
    @SerializedName("apellidos") val apellidos: String = "",
    @SerializedName("fechaNacimiento") val fechaNacimiento: String = "",
    @SerializedName("correo") val correo: String = "",
    @SerializedName("password") val password: String = "",
    @SerializedName("herramientas_propias") val herramientasPropias: String? = null,
    @SerializedName("calificacion_promedio") val calificacionPromedio: Double = 0.0,
    @SerializedName("rol") val rol: String = "USER"
)
