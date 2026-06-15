package com.example.vecizervi.data.repositories

import com.example.vecizervi.data.models.LoginRequest
import com.example.vecizervi.data.models.Usuario
import com.example.vecizervi.data.utils.ApiClient

// Resultado tipado del login para distinguir bloqueo de error simple
data class LoginResultado(
    val usuario: Usuario? = null,
    val bloqueada: Boolean = false,
    val mensaje: String = ""
)

class UserRepository {
    private var usuarioActual: Usuario? = null

    // Login simple (compatibilidad con código existente)
    suspend fun login(correo: String, password: String): Usuario? {
        return loginConError(correo, password).usuario
    }

    // S04: login con información detallada del error + guarda el token JWT
    suspend fun loginConError(correo: String, password: String): LoginResultado {
        return try {
            val response = ApiClient.servicioUsuarios.login(LoginRequest(correo, password))
            when {
                response.isSuccessful -> {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        // S04: guardar el token para que todas las peticiones siguientes lo usen
                        ApiClient.jwtToken = loginResponse.token
                        usuarioActual = loginResponse.usuario
                    }
                    LoginResultado(usuario = usuarioActual)
                }
                response.code() == 403 -> {
                    val msg = response.errorBody()?.string() ?: "Cuenta bloqueada."
                    LoginResultado(bloqueada = true, mensaje = msg)
                }
                response.code() == 401 -> {
                    val msg = response.errorBody()?.string() ?: "Credenciales incorrectas."
                    LoginResultado(mensaje = msg)
                }
                else -> LoginResultado(mensaje = "Error del servidor (${response.code()})")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoginResultado(mensaje = "No se pudo conectar al servidor.")
        }
    }

    suspend fun registrarUsuario(
        rut: String, nombres: String, apellidos: String,
        fechaNacimiento: String, correo: String, password: String
    ): Usuario? {
        return try {
            val nuevoUsuario = Usuario(
                rut = rut, nombres = nombres, apellidos = apellidos,
                fechaNacimiento = fechaNacimiento, correo = correo, password = password
            )
            val response = ApiClient.servicioUsuarios.registrarUsuario(nuevoUsuario)
            if (response.isSuccessful) {
                usuarioActual = response.body()
                usuarioActual
            } else null
        } catch (e: Exception) { e.printStackTrace(); null }
    }

    suspend fun actualizarUsuario(
        idUsuario: Int, rut: String, nombres: String, apellidos: String,
        fechaNacimiento: String, correo: String
    ): Usuario? {
        return try {
            val usuarioEditado = Usuario(
                idUsuario = idUsuario, rut = rut, nombres = nombres,
                apellidos = apellidos, fechaNacimiento = fechaNacimiento,
                correo = correo, password = usuarioActual?.password ?: ""
            )
            val response = ApiClient.servicioUsuarios.actualizarUsuario(idUsuario, usuarioEditado)
            if (response.isSuccessful) {
                usuarioActual = response.body()
                usuarioActual
            } else null
        } catch (e: Exception) { e.printStackTrace(); null }
    }

    suspend fun eliminarUsuario(idUsuario: Int): Boolean {
        return try {
            val response = ApiClient.servicioUsuarios.eliminarUsuario(idUsuario)
            if (response.isSuccessful && usuarioActual?.idUsuario == idUsuario) {
                usuarioActual = null
                ApiClient.jwtToken = null   // limpiar token al eliminar cuenta
            }
            response.isSuccessful
        } catch (e: Exception) { e.printStackTrace(); false }
    }

    suspend fun recuperarPassword(correo: String): String? {
        return try {
            val response = ApiClient.servicioUsuarios.recuperarPassword(correo)
            if (response.isSuccessful) response.body()?.get("codigo") else null
        } catch (e: Exception) { e.printStackTrace(); null }
    }

    suspend fun verificarToken(correo: String, token: String): Boolean {
        return try {
            ApiClient.servicioUsuarios.verificarToken(correo, token).isSuccessful
        } catch (e: Exception) { e.printStackTrace(); false }
    }

    suspend fun cambiarClave(correo: String, token: String, nuevaClave: String): Boolean {
        return try {
            ApiClient.servicioUsuarios.nuevaClave(correo, token, nuevaClave).isSuccessful
        } catch (e: Exception) { e.printStackTrace(); false }
    }

    fun obtenerUsuarioActual(): Usuario? = usuarioActual

    // S04: limpiar sesión completa incluyendo el token JWT
    fun logout() {
        usuarioActual = null
        ApiClient.jwtToken = null
    }
}
