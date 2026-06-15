package com.example.vecizervi.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vecizervi.data.models.Trabajo
import com.example.vecizervi.data.repositories.TrabajoRepository
import com.example.vecizervi.data.repositories.UserRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEditarTrabajo(
    navController: NavController,
    trabajoId: Long,
    trabajoRepo: TrabajoRepository,
    userRepo: UserRepository
) {
    var trabajo by remember { mutableStateOf<Trabajo?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var comunaSeleccionada by remember { mutableStateOf("") }
    var expandedComuna by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var mensajeExito by remember { mutableStateOf<String?>(null) }
    var guardando by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val usuarioActual = userRepo.obtenerUsuarioActual()

    val comunas = listOf("Maipú", "La Florida", "Santiago Centro", "Providencia", "Las Condes", "Ñuñoa", "Puente Alto")

    LaunchedEffect(trabajoId) {
        val t = trabajoRepo.obtenerTrabajoPorId(trabajoId)
        if (t != null) {
            trabajo = t
            titulo = t.titulo
            descripcion = t.descripcion
            precio = t.precio.toString()
            comunaSeleccionada = t.comuna
        }
        cargando = false
    }

    if (cargando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (trabajo == null) {
        Text("Trabajo no encontrado", modifier = Modifier.padding(16.dp))
        return
    }

    if (usuarioActual == null || usuarioActual.idUsuario != trabajo!!.idCliente) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("No tienes permiso para editar este trabajo", color = MaterialTheme.colorScheme.error)
            Button(onClick = { navController.popBackStack() }) { Text("Volver") }
        }
        return
    }

    // Diálogo confirmar eliminar
    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text("Eliminar trabajo") },
            text = { Text("¿Estás seguro que quieres eliminar este trabajo? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val eliminado = trabajoRepo.eliminarTrabajo(trabajoId)
                            if (eliminado) {
                                navController.navigate("inicio") {
                                    popUpTo("editar/$trabajoId") { inclusive = true }
                                }
                            } else {
                                mensajeError = "Error al eliminar el trabajo"
                            }
                            mostrarDialogoEliminar = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = false }) { Text("Cancelar") }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Editar Publicación", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
        OutlinedTextField(
            value = precio,
            onValueChange = { precio = it },
            label = { Text("Precio (\$)") },
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
            expanded = expandedComuna,
            onExpandedChange = { expandedComuna = !expandedComuna }
        ) {
            OutlinedTextField(
                value = comunaSeleccionada,
                onValueChange = {},
                readOnly = true,
                label = { Text("Comuna") },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedComuna,
                onDismissRequest = { expandedComuna = false }
            ) {
                comunas.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = { comunaSeleccionada = opcion; expandedComuna = false }
                    )
                }
            }
        }

        mensajeError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        mensajeExito?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

        Button(
            onClick = {
                val precioDouble = precio.toDoubleOrNull()
                when {
                    titulo.length < 5 -> mensajeError = "El título debe ser más descriptivo"
                    descripcion.isBlank() -> mensajeError = "La descripción no puede estar vacía"
                    precioDouble == null || precioDouble <= 0 -> mensajeError = "El precio debe ser un número mayor a 0"
                    else -> {
                        scope.launch {
                            guardando = true
                            val trabajoEditado = trabajo!!.copy(
                                titulo = titulo,
                                descripcion = descripcion,
                                precio = precioDouble,
                                comuna = comunaSeleccionada
                            )
                            val resultado = trabajoRepo.actualizarTrabajo(trabajoId, trabajoEditado)
                            if (resultado != null) {
                                mensajeExito = "¡Cambios guardados!"
                                mensajeError = null
                                navController.navigate("inicio") {
                                    popUpTo("editar/$trabajoId") { inclusive = true }
                                }
                            } else {
                                mensajeError = "Error al guardar cambios"
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
            else Text("Guardar cambios")
        }

        // Botón eliminar
        OutlinedButton(
            onClick = { mostrarDialogoEliminar = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Eliminar trabajo")
        }

        TextButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }
    }
}