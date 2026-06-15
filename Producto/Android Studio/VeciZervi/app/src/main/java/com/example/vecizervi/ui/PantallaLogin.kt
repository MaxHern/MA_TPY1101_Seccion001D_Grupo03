package com.example.vecizervi.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vecizervi.R
import com.example.vecizervi.data.repositories.UserRepository
import kotlinx.coroutines.launch

@Composable
fun PantallaLogin(navController: NavController, userRepo: UserRepository) {
    var correo          by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var mensajeError    by remember { mutableStateOf<String?>(null) }
    var cuentaBloqueada by remember { mutableStateOf(false) }
    var cargando        by remember { mutableStateOf(false) }
    val scope           = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_vecizervi),
            contentDescription = "Logo VeciZervi",
            modifier = Modifier.height(220.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text("Iniciar Sesión", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it; mensajeError = null; cuentaBloqueada = false },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; mensajeError = null; cuentaBloqueada = false },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Aviso de error o bloqueo ──────────────────────────────────────
        mensajeError?.let { msg ->
            if (cuentaBloqueada) {
                // Card especial para cuenta bloqueada
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "🔒 Cuenta Bloqueada",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { navController.navigate("recuperar") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Recuperar contraseña")
                        }
                    }
                }
            } else {
                Text(
                    msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                when {
                    correo.isBlank() || password.isBlank() ->
                        mensajeError = "Todos los campos son obligatorios"
                    else -> {
                        scope.launch {
                            cargando = true
                            mensajeError = null
                            cuentaBloqueada = false
                            val resultado = userRepo.loginConError(correo.trim(), password)
                            when {
                                resultado.usuario != null -> {
                                    navController.navigate("inicio") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                                resultado.bloqueada -> {
                                    cuentaBloqueada = true
                                    mensajeError = resultado.mensaje
                                }
                                else -> {
                                    mensajeError = resultado.mensaje
                                }
                            }
                            cargando = false
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        ) {
            if (cargando) CircularProgressIndicator(modifier = Modifier.size(20.dp))
            else Text("Ingresar")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { navController.navigate("register") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Registrarme") }

        TextButton(
            onClick = { navController.navigate("recuperar") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("¿Olvidaste tu contraseña?") }
    }
}