package com.example.vecizervi.data.repositories

import com.example.vecizervi.data.models.LoginRequest
import com.example.vecizervi.data.models.LoginResponse
import com.example.vecizervi.data.models.Usuario
import com.example.vecizervi.data.utils.ApiClient
import retrofit2.Response

class AuthRepository {
    private val servicioUsuarios = ApiClient.servicioUsuarios

    suspend fun registrarUsuario(usuario: Usuario): Response<Usuario> {
        return servicioUsuarios.registrarUsuario(usuario)
    }

    // S04: login ahora devuelve LoginResponse { usuario, token }
    suspend fun login(loginRequest: LoginRequest): Response<LoginResponse> {
        return servicioUsuarios.login(loginRequest)
    }
}
