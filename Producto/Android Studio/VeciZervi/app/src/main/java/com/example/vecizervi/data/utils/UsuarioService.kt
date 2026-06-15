package com.example.vecizervi.data.utils

import com.example.vecizervi.data.models.LoginRequest
import com.example.vecizervi.data.models.LoginResponse
import com.example.vecizervi.data.models.Usuario
import retrofit2.Response
import retrofit2.http.*

interface UsuarioService {

    // S04: login ahora devuelve { usuario, token }
    @POST("usuarios/login")
    suspend fun login(@Body credenciales: LoginRequest): Response<LoginResponse>

    @POST("usuarios/registro")
    suspend fun registrarUsuario(@Body usuario: Usuario): Response<Usuario>

    @GET("usuarios/{idUsuario}")
    suspend fun getUsuarioPorId(@Path("idUsuario") idUsuario: Int): Response<Usuario>

    @GET("usuarios")
    suspend fun getTodosUsuarios(): Response<List<Usuario>>

    @PUT("usuarios/{idUsuario}/perfil")
    suspend fun actualizarUsuario(
        @Path("idUsuario") idUsuario: Int,
        @Body usuario: Usuario
    ): Response<Usuario>

    @DELETE("usuarios/{idUsuario}")
    suspend fun eliminarUsuario(@Path("idUsuario") idUsuario: Int): Response<Unit>

    @POST("usuarios/recuperar-clave")
    suspend fun recuperarPassword(@Query("correo") correo: String): Response<Map<String, String>>

    @POST("usuarios/verificar-token")
    suspend fun verificarToken(
        @Query("correo") correo: String,
        @Query("token") token: String
    ): Response<Unit>

    @POST("usuarios/nueva-clave")
    suspend fun nuevaClave(
        @Query("correo") correo: String,
        @Query("token") token: String,
        @Query("nuevaClave") nuevaClave: String
    ): Response<Unit>
}
