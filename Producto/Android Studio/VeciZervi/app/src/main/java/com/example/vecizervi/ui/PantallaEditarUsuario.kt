package com.example.vecizervi.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vecizervi.data.models.Usuario
import com.example.vecizervi.data.utils.ApiClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEditarUsuario(
    navController: NavController,
    idUsuario: Int
) {
    var usuarioOriginal by remember { mutableStateOf<Usuario?>(null) }
    var nombres         by remember { mutableStateOf("") }
    var apellidos       by remember { mutableStateOf("") }
    var cargando        by remember { mutableStateOf(true) }
    var guardando       by remember { mutableStateOf(false) }
    var mensajeError    by remember { mutableStateOf<String?>(null) }
    var mensajeExito    by remember { mutableStateOf<String?>(null) }
    val scope           = rememberCoroutineScope()

    LaunchedEffect(idUsuario) {
        try {
            val response = ApiClient.servicioUsuarios.getUsuarioPorId(idUsuario)
            if (response.isSuccessful) {
                val u = response.body()
                if (u != null) {
                    usuarioOriginal = u
                    nombres   = u.nombres
                    apellidos = u.apellidos
                }
            } else {
                mensajeError = "No se pudo cargar el usuario (${response.code()})"
            }
        } catch (e: Exception) {
            mensajeError = "Error de conexión: ${e.message}"
        } finally {
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Usuario") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (cargando) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val u = usuarioOriginal
        if (u == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center) {
                Text("Usuario no encontrado", color = MaterialTheme.colorScheme.error)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Campos no editables
            Text("RUT", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Surface(modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraSmall,
                color = MaterialTheme.colorScheme.surfaceVariant) {
                Text(u.rut,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Text("Correo", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Surface(modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraSmall,
                color = MaterialTheme.colorScheme.surfaceVariant) {
                Text(u.correo,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Text("Fecha de nacimiento", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Surface(modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraSmall,
                color = MaterialTheme.colorScheme.surfaceVariant) {
                Text(u.fechaNacimiento.ifBlank { "No registrada" },
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
                                try {
                                    val usuarioActualizado = Usuario(
                                        idUsuario            = idUsuario,
                                        rut                  = u.rut,
                                        nombres              = nombres,
                                        apellidos            = apellidos,
                                        fechaNacimiento      = u.fechaNacimiento,
                                        correo               = u.correo,
                                        password             = u.password,
                                        rol                  = u.rol,
                                        calificacionPromedio = u.calificacionPromedio,
                                        herramientasPropias  = u.herramientasPropias
                                    )
                                    val response = ApiClient.servicioUsuarios.actualizarUsuario(
                                        idUsuario, usuarioActualizado
                                    )
                                    if (response.isSuccessful) {
                                        mensajeExito = "Usuario actualizado"
                                        mensajeError = null
                                        navController.popBackStack()
                                    } else {
                                        mensajeError = "Error al guardar (${response.code()})"
                                    }
                                } catch (e: Exception) {
                                    mensajeError = "Error: ${e.message}"
                                } finally {
                                    guardando = false
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !guardando
            ) {
                if (guardando) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else Text("Guardar cambios")
            }

            TextButton(onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar")
            }
        }
    }
}