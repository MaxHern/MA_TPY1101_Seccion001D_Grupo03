package com.example.vecizervi.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vecizervi.data.repositories.UserRepository
import com.example.vecizervi.data.utils.calcularEdad
import com.example.vecizervi.data.utils.validarPassword
import com.example.vecizervi.data.utils.validarRut
import kotlinx.coroutines.launch

@Composable
fun PantallaRegister(navController: NavController, userRepo: UserRepository) {
    var rut by remember { mutableStateOf("") }
    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var mensajeExito by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Registro de Usuario", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(value = rut, onValueChange = { rut = it }, label = { Text("RUT (ej: 12345678-9)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = nombres, onValueChange = { nombres = it }, label = { Text("Nombres") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = apellidos, onValueChange = { apellidos = it }, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = fechaNacimiento, onValueChange = { fechaNacimiento = it }, label = { Text("Fecha de nacimiento (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = correo, onValueChange = { correo = it }, label = { Text("Correo electrónico") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
        OutlinedTextField(value = confirmarPassword, onValueChange = { confirmarPassword = it }, label = { Text("Confirmar Contraseña") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())

        mensajeError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        mensajeExito?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

        Button(
            onClick = {
                when {
                    rut.isBlank() || nombres.isBlank() || apellidos.isBlank() || fechaNacimiento.isBlank() || correo.isBlank() || password.isBlank() -> {
                        mensajeError = "Todos los campos son obligatorios"
                    }
                    !validarRut(rut) -> {
                        mensajeError = "RUT inválido (formato: 12345678-9)"
                    }
                    !validarPassword(password) -> {
                        mensajeError = "La contraseña debe tener mínimo 8 caracteres, una mayúscula, un número y un símbolo"
                    }
                    password != confirmarPassword -> {
                        mensajeError = "Las contraseñas no coinciden"
                    }
                    else -> {
                        try {
                            val edad = calcularEdad(fechaNacimiento)
                            if (edad < 18) {
                                mensajeError = "Debes ser mayor de edad para registrarte"
                            } else {
                                scope.launch {
                                    cargando = true
                                    val resultado = userRepo.registrarUsuario(rut, nombres, apellidos, fechaNacimiento, correo, password)
                                    if (resultado != null) {
                                        mensajeExito = "¡Registro exitoso!"
                                        mensajeError = null
                                        navController.navigate("login") { popUpTo("register") { inclusive = true } }
                                    } else {
                                        mensajeError = "Error al registrar. Verifica que el correo y RUT no estén en uso."
                                    }
                                    cargando = false
                                }
                            }
                        } catch (e: Exception) {
                            mensajeError = "Formato de fecha inválido. Usa yyyy-MM-dd"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        ) {
            if (cargando) CircularProgressIndicator(modifier = Modifier.size(20.dp))
            else Text("Registrarse")
        }

        TextButton(onClick = { navController.navigate("login") }, modifier = Modifier.fillMaxWidth()) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}
