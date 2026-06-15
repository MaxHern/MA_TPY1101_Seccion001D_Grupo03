package com.example.vecizervi.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vecizervi.data.models.Trabajo
import com.example.vecizervi.data.models.Usuario
import com.example.vecizervi.data.repositories.UserRepository
import com.example.vecizervi.data.utils.ApiClient
import kotlinx.coroutines.launch

// FIX-29/30: campo noLeidos añadido
data class ConversacionInbox(
    val trabajo: Trabajo,
    val otroUsuario: Usuario,
    val esElDueno: Boolean,
    val noLeidos: Int = 0
)

@Composable
fun PantallaChatInbox(
    navController: NavController,
    userRepo: UserRepository
) {
    val usuarioActual  = userRepo.obtenerUsuarioActual()
    var conversaciones by remember { mutableStateOf<List<ConversacionInbox>>(emptyList()) }
    var cargando       by remember { mutableStateOf(true) }
    var errorMsg       by remember { mutableStateOf<String?>(null) }
    val scope          = rememberCoroutineScope()

    LaunchedEffect(usuarioActual) {
        if (usuarioActual == null) { cargando = false; return@LaunchedEffect }
        scope.launch {
            try {
                val miId = usuarioActual.idUsuario ?: 0
                val resultado = mutableListOf<ConversacionInbox>()

                val respInbox = ApiClient.servicioMensajes.getInbox(miId)
                val trabajos = if (respInbox.isSuccessful) respInbox.body() ?: emptyList() else emptyList()

                for (trabajo in trabajos) {
                    val clienteId = trabajo.cliente?.idUsuario ?: 0
                    val esElDueno = miId == clienteId

                    if (esElDueno) {
                        val respPart = ApiClient.servicioMensajes.getParticipantes(
                            trabajo.idTrabajo?.toInt() ?: 0, miId
                        )
                        val participantes = if (respPart.isSuccessful) respPart.body() ?: emptyList() else emptyList()

                        for (participante in participantes) {
                            // FIX-29/30: obtener cantidad de no leídos por conversación
                            val noLeidos = try {
                                val resp = ApiClient.servicioMensajes.getNoLeidosPorConversacion(
                                    trabajo.idTrabajo?.toInt() ?: 0, miId
                                )
                                if (resp.isSuccessful) resp.body()?.get("noLeidos") ?: 0 else 0
                            } catch (e: Exception) { 0 }

                            resultado.add(ConversacionInbox(trabajo, participante, true, noLeidos))
                        }
                    } else {
                        val dueno = Usuario(
                            idUsuario = clienteId,
                            nombres   = trabajo.cliente?.nombres ?: "",
                            apellidos = trabajo.cliente?.apellidos ?: ""
                        )
                        val yaExiste = resultado.any {
                            it.trabajo.idTrabajo == trabajo.idTrabajo && it.otroUsuario.idUsuario == clienteId
                        }
                        if (!yaExiste) {
                            // FIX-29/30: obtener cantidad de no leídos por conversación
                            val noLeidos = try {
                                val resp = ApiClient.servicioMensajes.getNoLeidosPorConversacion(
                                    trabajo.idTrabajo?.toInt() ?: 0, miId
                                )
                                if (resp.isSuccessful) resp.body()?.get("noLeidos") ?: 0 else 0
                            } catch (e: Exception) { 0 }

                            resultado.add(ConversacionInbox(trabajo, dueno, false, noLeidos))
                        }
                    }
                }

                conversaciones = resultado
            } catch (e: Exception) {
                errorMsg = "No se pudo conectar al servidor"
                e.printStackTrace()
            } finally {
                cargando = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Mensajes", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(12.dp))

        when {
            cargando -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            errorMsg != null -> {
                Text(errorMsg!!, color = MaterialTheme.colorScheme.error)
            }
            conversaciones.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No tienes conversaciones activas.\nAbre el chat desde el detalle de un trabajo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(conversaciones) { conv ->
                        val idTrabajo   = conv.trabajo.idTrabajo ?: 0
                        val otroUsuario = conv.otroUsuario
                        val nombreOtro  = "${otroUsuario.nombres} ${otroUsuario.apellidos}".trim()
                        val idOtro      = otroUsuario.idUsuario ?: 0

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate("chat/$nombreOtro/$idTrabajo/$idOtro")
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        conv.trabajo.titulo,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = if (conv.noLeidos > 0) FontWeight.ExtraBold else FontWeight.Bold
                                    )
                                    Text(
                                        if (conv.esElDueno) "Mensaje de: $nombreOtro"
                                        else "Publicado por: $nombreOtro",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = when (conv.trabajo.estado) {
                                            "Asignado"   -> MaterialTheme.colorScheme.primaryContainer
                                            "Finalizado" -> MaterialTheme.colorScheme.surfaceVariant
                                            else         -> MaterialTheme.colorScheme.secondaryContainer
                                        }
                                    ) {
                                        Text(
                                            conv.trabajo.estado,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }

                                // FIX-29/30: badge de mensajes no leídos
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (conv.noLeidos > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.padding(end = 8.dp)
                                        ) {
                                            Text(
                                                text = if (conv.noLeidos > 9) "9+" else conv.noLeidos.toString(),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onError
                                            )
                                        }
                                    }
                                    Text(
                                        "›",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
