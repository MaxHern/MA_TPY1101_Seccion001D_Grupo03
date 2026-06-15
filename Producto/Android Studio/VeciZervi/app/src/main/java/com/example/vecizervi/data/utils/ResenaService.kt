package com.example.vecizervi.data.utils

import com.example.vecizervi.data.models.Resena
import retrofit2.Response
import retrofit2.http.*

interface ResenaService {

    @GET("resenas/trabajo/{idTrabajo}")
    suspend fun getResenasPorTrabajo(@Path("idTrabajo") idTrabajo: Long): Response<List<Resena>>

    // NUEVO: reseñas recibidas por un usuario (para su perfil)
    @GET("resenas/usuario/{idUsuario}")
    suspend fun getResenasPorUsuario(@Path("idUsuario") idUsuario: Long): Response<List<Resena>>

    @POST("resenas")
    suspend fun crearResena(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<Resena>
}