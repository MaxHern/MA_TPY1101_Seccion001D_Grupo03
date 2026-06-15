package com.example.vecizervi.data.utils

import com.example.vecizervi.data.models.Mensaje
import com.example.vecizervi.data.models.Trabajo
import com.example.vecizervi.data.models.Usuario
import retrofit2.Response
import retrofit2.http.*

interface MensajeService {

    @GET("mensajes/{idTrabajo}")
    suspend fun getMensajes(
        @Path("idTrabajo") idTrabajo: Int,
        @Query("idUsuario1") idUsuario1: Int,
        @Query("idUsuario2") idUsuario2: Int
    ): Response<List<Mensaje>>

    @GET("mensajes/inbox/{idUsuario}")
    suspend fun getInbox(@Path("idUsuario") idUsuario: Int): Response<List<Trabajo>>

    @GET("mensajes/participantes/{idTrabajo}")
    suspend fun getParticipantes(
        @Path("idTrabajo") idTrabajo: Int,
        @Query("idDueno") idDueno: Int
    ): Response<List<Usuario>>

    // FIX-29/30: contar no leídos de una conversación específica
    @GET("mensajes/no-leidos/{idTrabajo}")
    suspend fun getNoLeidosPorConversacion(
        @Path("idTrabajo") idTrabajo: Int,
        @Query("idReceptor") idReceptor: Int
    ): Response<Map<String, Int>>

    @POST("mensajes")
    suspend fun postMensaje(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<Mensaje>
}
