package com.example.vecizervi.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vecizervi.data.models.Resena
import com.example.vecizervi.data.models.Trabajo
import com.example.vecizervi.data.repositories.TrabajoRepository
import com.example.vecizervi.data.repositories.UserRepository
import com.example.vecizervi.data.utils.ApiClient
import kotlinx.coroutines.launch

@Composable
fun PantallaPerfil(
    navController: NavController,
    userRepo: UserRepository,
    repo: TrabajoRepository = TrabajoRepository()
) {
    val usuarioActual = userRepo.obtenerUsuarioActual()
    var misTrabajos   by remember { mutableStateOf<List<Trabajo>>(emptyList()) }
    var resenas       by remember { mutableStateOf<List<Resena>>(emptyList()) }
    val scope         = rememberCoroutineScope()

    LaunchedEffect(usuarioActual) {
        if (usuarioActual != null) {
            // Cargar trabajos publicados
            try {
                misTrabajos = repo.obtenerTrabajosPorCliente((usuarioActual.idUsuario ?: 0).toLong())
            } catch (e: Exception) { e.printStackTrace() }

            // Cargar reseñas recibidas
            try {
                val resp = ApiClient.servicioResenas.getResenasPorUsuario(
                    (usuarioActual.idUsuario ?: 0).toLong()
                )
                if (resp.isSuccessful) resenas = resp.body() ?: emptyList()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // Calcular promedio localmente con las reseñas cargadas
    val promedioLocal = if (resenas.isNotEmpty())
        resenas.map { it.estrellas }.average() else usuarioActual?.calificacionPromedio ?: 0.0

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Perfil de Usuario", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (usuarioActual != null) {
            // ── Datos del usuario ──────────────────────────────────────
            item {
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("RUT: ${usuarioActual.rut}", style = MaterialTheme.typography.bodyMedium)
                        Text("Nombre: ${usuarioActual.nombres} ${usuarioActual.apellidos}",
                            style = MaterialTheme.typography.bodyMedium)
                        Text("Fecha de nacimiento: ${usuarioActual.fechaNacimiento}",
                            style = MaterialTheme.typography.bodyMedium)
                        Text("Correo: ${usuarioActual.correo}", style = MaterialTheme.typography.bodyMedium)

                        // Calificación promedio con estrellas visuales
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Calificación:", style = MaterialTheme.typography.bodyMedium)
                            repeat(5) { i ->
                                Icon(
                                    imageVector = if (i < promedioLocal.toInt()) Icons.Filled.Star
                                    else Icons.Outlined.StarOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text("(${"%.1f".format(promedioLocal)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // ── Botones ────────────────────────────────────────────────
            item {
                Button(onClick = { navController.navigate("editarPerfil") },
                    modifier = Modifier.fillMaxWidth()) { Text("Editar Perfil") }
            }
            item {
                Button(
                    onClick = {
                        userRepo.logout()
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Cerrar Sesión") }
            }

            // ── Reseñas recibidas ──────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Reseñas recibidas (${resenas.size})",
                    style = MaterialTheme.typography.titleMedium)
            }

            if (resenas.isEmpty()) {
                item {
                    Text("Aún no tienes reseñas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(resenas) { resena ->
                    Card(modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(1.dp)) {
                        Column(modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Nombre del autor de la reseña
                            val autorNombre = resena.emisorObj?.let { "${it.nombres} ${it.apellidos}" }
                                ?: "Usuario"
                            Text(autorNombre, style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary)
                            // Estrellas
                            Row {
                                repeat(5) { i ->
                                    Icon(
                                        imageVector = if (i < resena.estrellas) Icons.Filled.Star
                                        else Icons.Outlined.StarOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            if (resena.comentario.isNotBlank()) {
                                Text(resena.comentario, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // ── Mis publicaciones ──────────────────────────────────────
            if (misTrabajos.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Mis publicaciones (${misTrabajos.size})",
                        style = MaterialTheme.typography.titleMedium)
                }
                items(misTrabajos) { trabajo ->
                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(trabajo.titulo, style = MaterialTheme.typography.titleSmall)
                            Text("Estado: ${trabajo.estado}", style = MaterialTheme.typography.bodySmall)
                            Text("Precio: $${"%,.0f".format(trabajo.precio)}", style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { navController.navigate("editar/${trabajo.idTrabajo ?: 0}") }) {
                                    Text("Editar")
                                }
                                OutlinedButton(onClick = {
                                    scope.launch {
                                        val id = trabajo.idTrabajo ?: return@launch
                                        val ok = repo.eliminarTrabajo(id)
                                        if (ok) misTrabajos = misTrabajos.filter { it.idTrabajo != id }
                                    }
                                }) { Text("Eliminar") }
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Text("No hay usuario activo", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigate("login") },
                    modifier = Modifier.fillMaxWidth()) { Text("Iniciar Sesión") }
            }
        }
    }
}