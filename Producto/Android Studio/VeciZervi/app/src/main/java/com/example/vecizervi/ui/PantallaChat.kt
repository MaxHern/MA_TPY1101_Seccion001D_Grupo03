package com.example.vecizervi.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vecizervi.data.models.Mensaje
import com.example.vecizervi.data.repositories.TrabajoRepository
import com.example.vecizervi.data.repositories.UserRepository
import com.example.vecizervi.data.utils.ApiClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaChat(
    navController: NavController,
    userRepo: UserRepository,
    autorNombre: String?,
    idTrabajo: Int,
    idOtroUsuario: Int = 0,
    trabajoRepo: TrabajoRepository = TrabajoRepository()
) {
    val usuarioActual           = userRepo.obtenerUsuarioActual()
    val miId                    = usuarioActual?.idUsuario ?: 0
    var mensajeTexto            by remember { mutableStateOf("") }
    var mensajes                by remember { mutableStateOf<List<Mensaje>>(emptyList()) }
    var estadoTrabajo           by remember { mutableStateOf("Disponible") }
    var esElDueno               by remember { mutableStateOf(false) }
    var idDueno                 by remember { mutableStateOf(idOtroUsuario) }
    var mostrarDialogoAceptar   by remember { mutableStateOf(false) }
    var mostrarDialogoCancelar  by remember { mutableStateOf(false) }
    var mostrarDialogoFinalizar by remember { mutableStateOf(false) }
    var mensajeEstado           by remember { mutableStateOf<String?>(null) }
    // FIX-28: estado de conexión del polling
    var sinConexion             by remember { mutableStateOf(false) }
    val scope                   = rememberCoroutineScope()
    val listState               = rememberLazyListState()

    LaunchedEffect(idTrabajo) {
        try {
            val t = trabajoRepo.obtenerTrabajoPorId(idTrabajo.toLong())
            if (t != null) {
                estadoTrabajo = t.estado
                val clienteId = t.cliente?.idUsuario ?: 0
                esElDueno = miId == clienteId
                if (idDueno == 0) idDueno = clienteId
            }
        } catch (e: Exception) { e.printStackTrace() }

        fun cargarMensajes() {
            scope.launch {
                try {
                    if (miId != 0 && idDueno != 0) {
                        val r = ApiClient.servicioMensajes.getMensajes(idTrabajo, miId, idDueno)
                        if (r.isSuccessful) {
                            mensajes = r.body() ?: emptyList()
                            sinConexion = false
                        }
                    }
                } catch (e: Exception) {
                    sinConexion = true
                    e.printStackTrace()
                }
            }
        }
        cargarMensajes()

        // Polling cada 3 segundos — FIX-28: captura errores y muestra banner
        while (true) {
            delay(3000)
            try {
                if (miId != 0 && idDueno != 0) {
                    val r = ApiClient.servicioMensajes.getMensajes(idTrabajo, miId, idDueno)
                    if (r.isSuccessful) {
                        sinConexion = false
                        val nuevos = r.body() ?: emptyList()
                        if (nuevos.size != mensajes.size) mensajes = nuevos
                    }
                }
            } catch (e: Exception) {
                sinConexion = true
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(mensajes.size) {
        if (mensajes.isNotEmpty()) listState.animateScrollToItem(mensajes.size - 1)
    }

    // Diálogo Aceptar trabajo
    if (mostrarDialogoAceptar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoAceptar = false },
            title = { Text("Aceptar trabajo") },
            text  = { Text("¿Confirmas aceptar este trabajo? El estado cambiará a Asignado.") },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        try {
                            val t = trabajoRepo.obtenerTrabajoPorId(idTrabajo.toLong())
                            if (t != null) {
                                val ok = trabajoRepo.actualizarTrabajo(idTrabajo.toLong(), t.copy(estado = "Asignado"))
                                if (ok != null) { estadoTrabajo = "Asignado"; mensajeEstado = "✓ Trabajo aceptado" }
                                else mensajeEstado = "Error al aceptar"
                            }
                        } catch (e: Exception) { mensajeEstado = "Error: ${e.message}" }
                        mostrarDialogoAceptar = false
                    }
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoAceptar = false }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo Cancelar trabajo
    if (mostrarDialogoCancelar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCancelar = false },
            title = { Text("Cancelar trabajo") },
            text  = { Text("¿Estás seguro? El estado volverá a Disponible.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val t = trabajoRepo.obtenerTrabajoPorId(idTrabajo.toLong())
                                if (t != null) {
                                    val ok = trabajoRepo.actualizarTrabajo(idTrabajo.toLong(), t.copy(estado = "Disponible"))
                                    if (ok != null) { estadoTrabajo = "Disponible"; mensajeEstado = "Trabajo cancelado" }
                                    else mensajeEstado = "Error al cancelar"
                                }
                            } catch (e: Exception) { mensajeEstado = "Error: ${e.message}" }
                            mostrarDialogoCancelar = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCancelar = false }) { Text("No cancelar") }
            }
        )
    }

    // Diálogo Finalizar trabajo
    if (mostrarDialogoFinalizar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoFinalizar = false },
            title = { Text("Finalizar trabajo") },
            text  = { Text("¿Confirmas que el trabajo fue realizado con éxito? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        try {
                            val response = ApiClient.servicioTrabajos.finalizarTrabajo(idTrabajo.toLong())
                            if (response.isSuccessful) {
                                estadoTrabajo = "Finalizado"
                                mensajeEstado = "✓ Trabajo finalizado correctamente"
                            } else {
                                mensajeEstado = "Error al finalizar (${response.code()})"
                            }
                        } catch (e: Exception) {
                            mensajeEstado = "Error: ${e.message}"
                        }
                        mostrarDialogoFinalizar = false
                    }
                }) { Text("Sí, finalizar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoFinalizar = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(autorNombre ?: "Chat")
                        Text(
                            "Estado: $estadoTrabajo",
                            style = MaterialTheme.typography.labelSmall,
                            color = when (estadoTrabajo) {
                                "Asignado"   -> MaterialTheme.colorScheme.primary
                                "Finalizado" -> MaterialTheme.colorScheme.tertiary
                                else         -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(8.dp)) {

            // Botones de acción (solo para el dueño)
            if (esElDueno) {
                mensajeEstado?.let {
                    Text(it,
                        color = if (it.contains("Error")) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 4.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (estadoTrabajo == "Disponible") {
                        Button(
                            onClick = { mostrarDialogoAceptar = true },
                            modifier = Modifier.weight(1f)
                        ) { Text("Aceptar trabajo") }
                    }
                    if (estadoTrabajo == "Asignado") {
                        OutlinedButton(
                            onClick = { mostrarDialogoCancelar = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) { Text("Cancelar") }
                        Button(
                            onClick = { mostrarDialogoFinalizar = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) { Text("Finalizar ✓") }
                    }
                    if (estadoTrabajo == "Finalizado") {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                "✓ Este trabajo fue finalizado",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
            }

            // FIX-28: banner "Sin conexión" cuando el polling falla
            if (sinConexion) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        "Sin conexión — los mensajes podrían no estar actualizados",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Lista de mensajes
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(mensajes) { mensaje ->
                    val esYo = mensaje.idEmisorResuelto == miId
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (esYo) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = if (esYo) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                if (!esYo && mensaje.emisorObj != null) {
                                    Text(
                                        "${mensaje.emisorObj.nombres} ${mensaje.emisorObj.apellidos}".trim(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    mensaje.contenido,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (esYo) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                                // Fila con hora + palomita de visto (solo en mis mensajes)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        mensaje.fechaEnvio.take(16).replace("T", " "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (esYo) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    if (esYo) {
                                        Text(
                                            // ✓ = enviado, ✓✓ = visto (como WhatsApp)
                                            text = if (mensaje.leido) "✓✓" else "✓",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (mensaje.leido)
                                                MaterialTheme.colorScheme.onPrimary          // blanco intenso = visto
                                            else
                                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f) // tenue = no visto
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Input de mensaje
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = mensajeTexto,
                    onValueChange = { mensajeTexto = it },
                    placeholder = { Text(if (estadoTrabajo == "Finalizado") "Trabajo finalizado" else "Escribe un mensaje...") },
                    modifier = Modifier.weight(1f),
                    maxLines = 3,
                    enabled = estadoTrabajo != "Finalizado"
                )
                IconButton(
                    onClick = {
                        if (mensajeTexto.isNotBlank() && usuarioActual != null && idDueno != 0) {
                            val texto = mensajeTexto.trim()
                            mensajeTexto = ""
                            scope.launch {
                                try {
                                    val body = mapOf<String, Any>(
                                        "id_trabajo"  to idTrabajo,
                                        "id_emisor"   to miId,
                                        "id_receptor" to idDueno,
                                        "contenido"   to texto
                                    )
                                    val response = ApiClient.servicioMensajes.postMensaje(body)
                                    if (response.isSuccessful) {
                                        response.body()?.let { mensajes = mensajes + it }
                                        sinConexion = false
                                    }
                                } catch (e: Exception) {
                                    sinConexion = true
                                    e.printStackTrace()
                                }
                            }
                        }
                    },
                    enabled = estadoTrabajo != "Finalizado"
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar",
                        tint = if (estadoTrabajo != "Finalizado") MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
