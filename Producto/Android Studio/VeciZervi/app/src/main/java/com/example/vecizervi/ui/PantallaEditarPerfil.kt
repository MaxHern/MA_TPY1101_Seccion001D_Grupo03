package com.example.vecizervi.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vecizervi.data.repositories.UserRepository
import kotlinx.coroutines.launch

@Composable
fun PantallaEditarPerfil(navController: NavController, userRepo: UserRepository) {
    val usuarioActual = userRepo.obtenerUsuarioActual()
    if (usuarioActual == null) {
        LaunchedEffect(Unit) { navController.navigate("login") }
        return
    }

    var nombres         by remember { mutableStateOf(usuarioActual.nombres) }
    var apellidos       by remember { mutableStateOf(usuarioActual.apellidos) }
    var mensajeError    by remember { mutableStateOf<String?>(null) }
    var mensajeExito    by remember { mutableStateOf<String?>(null) }
    var guardando       by remember { mutableStateOf(false) }
    val scope           = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Editar Perfil", style = MaterialTheme.typography.headlineMedium)

        // Campos no editables — solo informativos
        Text("RUT", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraSmall,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(usuarioActual.rut,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Text("Correo electrónico", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraSmall,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(usuarioActual.correo,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Text("Fecha de nacimiento", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraSmall,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(usuarioActual.fechaNacimiento.ifBlank { "No registrada" },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Campos editables
        OutlinedTextField(value = nombres, onValueChange = { nombres = it },
            label = { Text("Nombres") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = apellidos, onValueChange = { apellidos = it },
            label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth())

        mensajeError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        mensajeExito?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

        Button(
            onClick = {
                when {
                    nombres.isBlank() || apellidos.isBlank() ->
                        mensajeError = "Nombres y apellidos son obligatorios"
                    else -> {
                        scope.launch {
                            guardando = true
                            val resultado = userRepo.actualizarUsuario(
                                usuarioActual.idUsuario ?: 0,
                                usuarioActual.rut,
                                nombres,
                                apellidos,
                                usuarioActual.fechaNacimiento,
                                usuarioActual.correo
                            )
                            if (resultado != null) {
                                mensajeExito = "Perfil actualizado exitosamente"
                                mensajeError = null
                                navController.navigate("perfil") {
                                    popUpTo("editarPerfil") { inclusive = true }
                                }
                            } else {
                                mensajeError = "Error al actualizar perfil"
                            }
                            guardando = false
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !guardando
        ) {
            if (guardando) CircularProgressIndicator(modifier = Modifier.size(20.dp))
            else Text("Guardar Cambios")
        }

        TextButton(onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()) {
            Text("Cancelar")
        }
    }
}