package com.example.vecizervi.data.models

import com.google.gson.annotations.SerializedName

data class Postulacion(
    @SerializedName("id_postulacion") val idPostulacion: Long? = null,
    @SerializedName("id_trabajo") val idTrabajo: Long,
    @SerializedName("id_trabajador") val idTrabajador: Long,
    @SerializedName("mensaje_presentacion") val mensajePresentacion: String = "",
    @SerializedName("fecha_postulacion") val fechaPostulacion: String = ""
)
