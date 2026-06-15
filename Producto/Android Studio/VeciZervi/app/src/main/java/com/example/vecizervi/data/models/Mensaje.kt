package com.example.vecizervi.data.models

import com.google.gson.annotations.SerializedName

data class EmisorResumen(
    @SerializedName("idUsuario") val idUsuario: Int = 0,
    @SerializedName("nombres")   val nombres: String = "",
    @SerializedName("apellidos") val apellidos: String = ""
)

data class Mensaje(
    @SerializedName("id")        val idMensaje: Long? = null,
    @SerializedName("emisor")    val emisorObj: EmisorResumen? = null,
    @SerializedName("receptor")  val receptorObj: EmisorResumen? = null,
    @SerializedName("id_trabajo")  val idTrabajo: Int = 0,
    @SerializedName("id_emisor")   val idEmisor: Int = 0,
    @SerializedName("id_receptor") val idReceptor: Int = 0,
    @SerializedName("contenido")   val contenido: String = "",
    @SerializedName("fechaEnvio")  val fechaEnvio: String = "",
    @SerializedName("leido")       val leido: Boolean = false
) {
    val idEmisorResuelto: Int get() = emisorObj?.idUsuario?.takeIf { it != 0 } ?: idEmisor
}