package com.example.vecizervi.data.models

import com.google.gson.annotations.SerializedName

data class ClienteResumen(
    @SerializedName("idUsuario") val idUsuario: Int? = null,
    @SerializedName("nombres")   val nombres: String = "",
    @SerializedName("apellidos") val apellidos: String = ""
)

data class CategoriaResumen(
    @SerializedName("id") val idCategoria: Int? = null,
    @SerializedName("nombreCategoria") val nombreCategoria: String = ""
)

data class Trabajo(
    @SerializedName("id") val idTrabajo: Long? = null,
    @SerializedName("cliente")            val cliente: ClienteResumen? = null,
    @SerializedName("categoria")          val categoria: CategoriaResumen? = null,
    @SerializedName("titulo")             val titulo: String = "",
    @SerializedName("descripcion")        val descripcion: String = "",
    @SerializedName("comuna")             val comuna: String = "",
    @SerializedName("precio")             val precio: Double = 0.0,
    @SerializedName("estado")             val estado: String = "Disponible",
    @SerializedName("fecha_publicacion")  val fechaPublicacion: String = "",
    @SerializedName("fecha_finalizacion") val fechaFinalizacion: String? = null,
    @SerializedName("latitud")            val latitud: Double? = null,
    @SerializedName("longitud")           val longitud: Double? = null
) {
    val idCliente: Int get() = cliente?.idUsuario ?: 0
}