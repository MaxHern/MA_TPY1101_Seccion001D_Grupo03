package com.example.vecizervi.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vecizervi.data.models.Trabajo
import com.example.vecizervi.data.models.Usuario
import com.example.vecizervi.data.repositories.UserRepository
import com.example.vecizervi.data.utils.ApiClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAdmin(navController: NavController, userRepo: UserRepository) {
    val usuarioActual = userRepo.obtenerUsuarioActual()

    if (usuarioActual?.rol != "ADMIN") {
        LaunchedEffect(Unit) { navController.navigate("inicio") }
        return
    }

    var tabSeleccionado by remember { mutableStateOf(0) }
    var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var trabajos by remember { mutableStateOf<List<Trabajo>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var usuarioAEliminar by remember { mutableStateOf<Usuario?>(null) }
    var trabajoAEliminar by remember { mutableStateOf<Trabajo?>(null) }
    val scope = rememberCoroutineScope()

    fun recargarDatos() {
        scope.launch {
            cargando = true
            try {
                val respUsuarios = ApiClient.servicioUsuarios.getTodosUsuarios()
                if (respUsuarios.isSuccessful) usuarios = respUsuarios.body() ?: emptyList()

                val respTrabajos = ApiClient.servicioTrabajos.getTodosLosTrabajos()
                if (respTrabajos.isSuccessful) trabajos = respTrabajos.body() ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) { recargarDatos() }

    // Diálogo eliminar usuario
    usuarioAEliminar?.let { usuario ->
        AlertDialog(
            onDismissRequest = { usuarioAEliminar = null },
            title = { Text("Eliminar usuario") },
            text = { Text("¿Eliminar a ${usuario.nombres} ${usuario.apellidos}? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val id = usuario.idUsuario?.toLong() ?: return@launch
                            val resp = ApiClient.servicioUsuarios.eliminarUsuario(id.toInt())
                            if (resp.isSuccessful) recargarDatos()
                            usuarioAEliminar = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { usuarioAEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo eliminar trabajo
    trabajoAEliminar?.let { trabajo ->
        AlertDialog(
            onDismissRequest = { trabajoAEliminar = null },
            title = { Text("Eliminar trabajo") },
            text = { Text("¿Eliminar '${trabajo.titulo}'? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val id = trabajo.idTrabajo ?: return@launch
                            val resp = ApiClient.servicioTrabajos.eliminarTrabajo(id)
                            if (resp.isSuccessful) recargarDatos()
                            trabajoAEliminar = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { trabajoAEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Panel de Administración") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = tabSeleccionado) {
                Tab(
                    selected = tabSeleccionado == 0,
                    onClick = { tabSeleccionado = 0 },
                    text = { Text("Usuarios (${usuarios.size})") }
                )
                Tab(
                    selected = tabSeleccionado == 1,
                    onClick = { tabSeleccionado = 1 },
                    text = { Text("Trabajos (${trabajos.size})") }
                )
            }

            if (cargando) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            } else {
                when (tabSeleccionado) {
                    0 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(usuarios) { usuario ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "${usuario.nombres} ${usuario.apellidos}",
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Surface(
                                                shape = MaterialTheme.shapes.small,
                                                color = if (usuario.rol == "ADMIN")
                                                    MaterialTheme.colorScheme.errorContainer
                                                else
                                                    MaterialTheme.colorScheme.secondaryContainer
                                            ) {
                                                Text(
                                                    usuario.rol,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }
                                        Text(usuario.correo, style = MaterialTheme.typography.bodySmall)
                                        Text("RUT: ${usuario.rut}", style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            "ID: ${usuario.idUsuario}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        // No permitir eliminar al propio admin
                                        if (usuario.idUsuario != usuarioActual.idUsuario) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {

                                                // BOTÓN EDITAR
                                                Button(
                                                    onClick = {
                                                        navController.navigate("editar_usuario/${usuario.idUsuario}")
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Editar")
                                                }

                                                // BOTÓN ELIMINAR
                                                OutlinedButton(
                                                    onClick = { usuarioAEliminar = usuario },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        contentColor = MaterialTheme.colorScheme.error
                                                    )
                                                ) {
                                                    Text("Eliminar")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(trabajos) { trabajo ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                trabajo.titulo,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                "$${"%,.0f".format(trabajo.precio)}",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        trabajo.cliente?.let {
                                            Text(
                                                "Cliente: ${it.nombres} ${it.apellidos}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        Text(
                                            "Comuna: ${trabajo.comuna}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            trabajo.categoria?.nombreCategoria?.let { cat ->
                                                Surface(
                                                    shape = MaterialTheme.shapes.small,
                                                    color = MaterialTheme.colorScheme.secondaryContainer
                                                ) {
                                                    Text(
                                                        cat,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                            }
                                            Surface(
                                                shape = MaterialTheme.shapes.small,
                                                color = when (trabajo.estado) {
                                                    "Disponible" -> MaterialTheme.colorScheme.tertiaryContainer
                                                    "Asignado" -> MaterialTheme.colorScheme.primaryContainer
                                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                                }
                                            ) {
                                                Text(
                                                    trabajo.estado,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    navController.navigate("editar_trabajo/${trabajo.idTrabajo}")
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Editar")
                                            }
                                            OutlinedButton(
                                                onClick = { trabajoAEliminar = trabajo },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.error
                                                )
                                            ) { Text("Eliminar") }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}