package com.example.vecizervi.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vecizervi.data.models.Resena
import com.example.vecizervi.data.repositories.TrabajoRepository
import com.example.vecizervi.data.repositories.UserRepository
import com.example.vecizervi.data.utils.ApiClient
import kotlinx.coroutines.launch

// Coordenadas de referencia (centro de Santiago)
private const val LAT_REF_DETALLE = -33.4489
private const val LON_REF_DETALLE = -70.6693

private val COMUNAS_COORDS = mapOf(
    "Maipú" to Pair(-33.5122, -70.7653),
    "La Florida" to Pair(-33.5167, -70.5833),
    "Santiago Centro" to Pair(-33.4489, -70.6693),
    "Santiago" to Pair(-33.4489, -70.6693),
    "Providencia" to Pair(-33.4333, -70.6167),
    "Las Condes" to Pair(-33.4167, -70.5833),
    "Ñuñoa" to Pair(-33.4500, -70.6000),
    "Puente Alto" to Pair(-33.6000, -70.5833),
    "Cerrillos" to Pair(-33.4956, -70.7272),
    "Estación Central" to Pair(-33.4558, -70.6817),
    "San Miguel" to Pair(-33.4981, -70.6478)
)

private fun esCoordenadaEnChile(lat: Double?, lon: Double?): Boolean {
    if (lat == null || lon == null || lat == 0.0 || lon == 0.0) return false
    return lat in -56.0..-17.0 && lon in -75.0..-66.0
}

private fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
}

private fun formatDistancia(km: Double): String =
    if (km < 1.0) "${(km * 1000).toInt()} m de distancia"
    else "${"%.1f".format(km)} km de distancia"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleTrabajo(
    navController: NavController,
    trabajoId: Long,
    userRepo: UserRepository,
    trabajoRepo: TrabajoRepository = TrabajoRepository()
) {
    var cargando               by remember { mutableStateOf(true) }
    var mensajePostulacion     by remember { mutableStateOf("") }
    var mostrarDialogo         by remember { mutableStateOf(false) }
    var mensajeResultado       by remember { mutableStateOf<String?>(null) }
    val scope                  = rememberCoroutineScope()
    val usuarioActual          = userRepo.obtenerUsuarioActual()
    val trabajoState           = remember { mutableStateOf<com.example.vecizervi.data.models.Trabajo?>(null) }

    var resenas                by remember { mutableStateOf<List<Resena>>(emptyList()) }
    var mostrarFormResena      by remember { mutableStateOf(false) }
    var estrellasSeleccionadas by remember { mutableIntStateOf(5) }
    var comentarioResena       by remember { mutableStateOf("") }
    var enviandoResena         by remember { mutableStateOf(false) }
    var mensajeResena          by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(trabajoId) {
        val t = trabajoRepo.obtenerTrabajoPorId(trabajoId)
        trabajoState.value = t
        cargando = false
        try {
            val resp = ApiClient.servicioResenas.getResenasPorTrabajo(trabajoId)
            if (resp.isSuccessful) resenas = resp.body() ?: emptyList()
        } catch (e: Exception) { e.printStackTrace() }
    }

    // Diálogo postular
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Postular al trabajo") },
            text = {
                OutlinedTextField(
                    value = mensajePostulacion,
                    onValueChange = { mensajePostulacion = it },
                    label = { Text("Mensaje de presentación") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (usuarioActual != null) {
                        scope.launch {
                            try {
                                val ok = trabajoRepo.postularTrabajo(
                                    trabajoId,
                                    usuarioActual.idUsuario?.toLong() ?: 0L,
                                    mensajePostulacion
                                )
                                mensajeResultado = if (ok) "¡Postulación enviada!" else "Error al postular"
                            } catch (e: Exception) {
                                mensajeResultado = "Error: ${e.message}"
                            }
                            mostrarDialogo = false
                        }
                    }
                }) { Text("Enviar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (cargando) "Cargando..." else trabajoState.value?.titulo ?: "Detalle") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            cargando -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            trabajoState.value == null -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center) {
                    Text("Trabajo no encontrado")
                }
            }
            else -> {
                val t = trabajoState.value!!
                val esElDueno = usuarioActual?.idUsuario != null &&
                        usuarioActual.idUsuario == t.cliente?.idUsuario

                // SCRUM-19: calcular distancia para mostrar en detalle
                val distanciaTexto: String? = run {
                    val gpsValido = esCoordenadaEnChile(t.latitud, t.longitud)
                    when {
                        gpsValido -> formatDistancia(
                            calcularDistancia(LAT_REF_DETALLE, LON_REF_DETALLE, t.latitud!!, t.longitud!!)
                        )
                        COMUNAS_COORDS.containsKey(t.comuna) -> {
                            val c = COMUNAS_COORDS[t.comuna]!!
                            formatDistancia(calcularDistancia(LAT_REF_DETALLE, LON_REF_DETALLE, c.first, c.second))
                        }
                        else -> null
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(t.titulo, style = MaterialTheme.typography.headlineSmall)
                    HorizontalDivider()

                    Text("Descripción:", style = MaterialTheme.typography.labelLarge)
                    Text(t.descripcion, style = MaterialTheme.typography.bodyMedium)
                    Text("Precio: \$${"%,.0f".format(t.precio)}", style = MaterialTheme.typography.bodyLarge)
                    Text("Comuna: ${t.comuna}", style = MaterialTheme.typography.bodyMedium)
                    Text("Estado: ${t.estado}", style = MaterialTheme.typography.bodyMedium)
                    t.categoria?.nombreCategoria?.let {
                        Text("Categoría: $it", style = MaterialTheme.typography.bodyMedium)
                    }
                    t.cliente?.let {
                        Text("Publicado por: ${it.nombres} ${it.apellidos}",
                            style = MaterialTheme.typography.bodySmall)
                    }

                    // SCRUM-19: mostrar distancia aproximada
                    distanciaTexto?.let { dist ->
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                "📍 $dist",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    mensajeResultado?.let {
                        Text(it, color = if (it.contains("Error"))
                            MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    }

                    if (usuarioActual != null && !esElDueno && t.estado == "Disponible") {
                        Button(onClick = { mostrarDialogo = true },
                            modifier = Modifier.fillMaxWidth()) {
                            Text("Postularme a este trabajo")
                        }
                    }

                    if (usuarioActual != null) {
                        OutlinedButton(
                            onClick = { navController.navigate("chat/${t.titulo}/${t.idTrabajo ?: 0}/${t.cliente?.idUsuario ?: 0}") },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Abrir Chat") }
                    }

                    if (esElDueno) {
                        Button(
                            onClick = { navController.navigate("editar/${t.idTrabajo ?: 0}") },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Editar publicación") }
                    }

                    // ── RESEÑAS ───────────────────────────────────────────
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Text("Reseñas", style = MaterialTheme.typography.titleMedium)

                    if (resenas.isEmpty()) {
                        Text("Aún no hay reseñas para este trabajo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        resenas.forEach { resena ->
                            Card(modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(1.dp)) {
                                Column(modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row {
                                        repeat(5) { i ->
                                            Icon(
                                                imageVector = if (i < resena.estrellas)
                                                    Icons.Filled.Star else Icons.Outlined.StarOutline,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    if (resena.comentario.isNotBlank()) {
                                        Text(resena.comentario, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }

                    // Formulario reseña (solo si no eres el dueño)
                    if (usuarioActual != null && !esElDueno) {
                        Spacer(modifier = Modifier.height(4.dp))
                        if (!mostrarFormResena) {
                            OutlinedButton(
                                onClick = { mostrarFormResena = true },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Dejar una reseña") }
                        } else {
                            Card(modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(2.dp)) {
                                Column(modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Tu reseña", style = MaterialTheme.typography.titleSmall)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        repeat(5) { i ->
                                            IconButton(
                                                onClick = { estrellasSeleccionadas = i + 1 },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (i < estrellasSeleccionadas)
                                                        Icons.Filled.Star else Icons.Outlined.StarOutline,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }
                                        Text("$estrellasSeleccionadas/5",
                                            modifier = Modifier.align(Alignment.CenterVertically),
                                            style = MaterialTheme.typography.bodySmall)
                                    }
                                    OutlinedTextField(
                                        value = comentarioResena,
                                        onValueChange = { comentarioResena = it },
                                        label = { Text("Escribe tu comentario...") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 2
                                    )
                                    mensajeResena?.let {
                                        Text(it, color = if (it.contains("Error"))
                                            MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.bodySmall)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    enviandoResena = true
                                                    try {
                                                        val body = mapOf(
                                                            "id_trabajo"  to trabajoId,
                                                            "id_emisor"   to (usuarioActual.idUsuario?.toLong() ?: 0L),
                                                            "id_receptor" to (t.cliente?.idUsuario?.toLong() ?: 0L),
                                                            "estrellas"   to estrellasSeleccionadas,
                                                            "comentario"  to comentarioResena
                                                        )
                                                        val resp = ApiClient.servicioResenas.crearResena(body)
                                                        if (resp.isSuccessful) {
                                                            mensajeResena = "¡Reseña enviada!"
                                                            resp.body()?.let { nueva -> resenas = resenas + nueva }
                                                            comentarioResena = ""
                                                            estrellasSeleccionadas = 5
                                                            mostrarFormResena = false
                                                        } else {
                                                            mensajeResena = "Error al enviar (${resp.code()})"
                                                        }
                                                    } catch (e: Exception) {
                                                        mensajeResena = "Error: ${e.message}"
                                                    } finally {
                                                        enviandoResena = false
                                                    }
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            enabled = !enviandoResena
                                        ) {
                                            if (enviandoResena)
                                                CircularProgressIndicator(modifier = Modifier.size(18.dp))
                                            else Text("Enviar")
                                        }
                                        OutlinedButton(
                                            onClick = { mostrarFormResena = false },
                                            modifier = Modifier.weight(1f)
                                        ) { Text("Cancelar") }
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