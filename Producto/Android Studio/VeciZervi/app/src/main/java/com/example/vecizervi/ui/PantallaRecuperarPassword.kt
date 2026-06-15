package com.example.vecizervi.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vecizervi.R
import com.example.vecizervi.data.repositories.UserRepository
import com.example.vecizervi.data.utils.validarPassword
import kotlinx.coroutines.launch

@Composable
fun PantallaRecuperarPassword(navController: NavController, userRepo: UserRepository) {

    var paso       by remember { mutableStateOf(1) }
    var correo     by remember { mutableStateOf("") }
    var codigoMostrado by remember { mutableStateOf("") }  // código que devuelve el backend
    var codigoIngresado by remember { mutableStateOf("") } // lo que escribe el usuario
    var nuevaClave by remember { mutableStateOf("") }
    var confirmar  by remember { mutableStateOf("") }
    var error      by remember { mutableStateOf<String?>(null) }
    var cargando   by remember { mutableStateOf(false) }
    val scope      = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_vecizervi),
            contentDescription = "Logo",
            modifier = Modifier.height(140.dp)
        )
        Spacer(Modifier.height(16.dp))

        Text("Recuperar Contraseña", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            "Paso $paso de 3",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(24.dp))

        // ── PASO 1: Ingresar correo ──────────────────────────────────────
        if (paso == 1) {
            Text(
                "Ingresa el correo asociado a tu cuenta.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it; error = null },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (correo.isBlank()) { error = "Ingresa tu correo"; return@Button }
                    scope.launch {
                        cargando = true; error = null
                        val codigo = userRepo.recuperarPassword(correo.trim())
                        if (codigo != null) {
                            codigoMostrado = codigo
                            paso = 2
                        } else {
                            // FIX-08: mensaje genérico — no revela si el correo existe o no
                            error = "Si el correo es válido, recibirás el código de recuperación."
                        }
                        cargando = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !cargando
            ) {
                if (cargando) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else Text("Generar código")
            }
        }

        // ── PASO 2: Mostrar código y pedirlo ─────────────────────────────
        if (paso == 2) {

            // Cuadro destacado con el código
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Tu código de recuperación:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        codigoMostrado,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        letterSpacing = 8.dp.value.toInt().sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "Ingresa el código de arriba para continuar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = codigoIngresado,
                onValueChange = { if (it.length <= 6) { codigoIngresado = it; error = null } },
                label = { Text("Código de 6 dígitos") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (codigoIngresado.length != 6) {
                        error = "El código debe tener 6 dígitos"; return@Button
                    }
                    scope.launch {
                        cargando = true; error = null
                        val ok = userRepo.verificarToken(correo.trim(), codigoIngresado.trim())
                        if (ok) paso = 3 else error = "Código incorrecto. Revísalo e intenta de nuevo."
                        cargando = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !cargando
            ) {
                if (cargando) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else Text("Verificar código")
            }
        }

        // ── PASO 3: Nueva contraseña ─────────────────────────────────────
        if (paso == 3) {
            Text(
                "¡Código verificado! Crea tu nueva contraseña.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = nuevaClave,
                onValueChange = { nuevaClave = it; error = null },
                label = { Text("Nueva contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = confirmar,
                onValueChange = { confirmar = it; error = null },
                label = { Text("Confirmar contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    when {
                        !validarPassword(nuevaClave) ->
                            error = "Mínimo 8 caracteres, una mayúscula, un número y un símbolo"
                        nuevaClave != confirmar ->
                            error = "Las contraseñas no coinciden"
                        else -> scope.launch {
                            cargando = true; error = null
                            val ok = userRepo.cambiarClave(correo.trim(), codigoIngresado.trim(), nuevaClave)
                            if (ok) {
                                navController.navigate("login") {
                                    popUpTo("recuperar") { inclusive = true }
                                }
                            } else {
                                error = "Error al cambiar contraseña. Intenta desde el paso 1."
                            }
                            cargando = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !cargando
            ) {
                if (cargando) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else Text("Guardar nueva contraseña")
            }
        }

        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(16.dp))
        TextButton(onClick = { navController.popBackStack() }) {
            Text("Volver al login")
        }
    }
}

// Extensión para letter spacing en sp
private val Int.sp get() = androidx.compose.ui.unit.TextUnit(this.toFloat(), androidx.compose.ui.unit.TextUnitType.Sp)