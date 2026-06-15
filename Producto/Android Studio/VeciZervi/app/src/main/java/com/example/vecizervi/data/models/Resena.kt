package com.example.vecizervi.data.models

import com.google.gson.annotations.SerializedName

data class ResenaEmisor(
    @SerializedName("idUsuario") val idUsuario: Int = 0,
    @SerializedName("nombres")   val nombres: String = "",
    @SerializedName("apellidos") val apellidos: String = ""
)

data class Resena(
    // El backend devuelve "id" no "id_resena"
    @SerializedName("id")                 val idResena: Long? = null,
    // Campos para enviar (POST)
    @SerializedName("id_trabajo")         val idTrabajo: Long = 0,
    @SerializedName("id_emisor")          val idEmisor: Long = 0,
    @SerializedName("id_receptor")        val idReceptor: Long = 0,
    // Campos que devuelve el backend (GET)
    @SerializedName("emisor")             val emisorObj: ResenaEmisor? = null,
    @SerializedName("receptor")           val receptorObj: ResenaEmisor? = null,
    @SerializedName("estrellas")          val estrellas: Int = 5,
    @SerializedName("comentario")         val comentario: String = "",
    @SerializedName("url_foto_evidencia") val urlFotoEvidencia: String? = null
)