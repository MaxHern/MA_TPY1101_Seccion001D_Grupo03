package com.example.vecizervi.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.vecizervi.data.models.Trabajo
import com.example.vecizervi.data.repositories.UserRepository
import com.example.vecizervi.data.utils.ApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

val mapaCategorias = mapOf(
    "Fletes" to 1, "Aseo doméstico" to 2, "Jardinería" to 3,
    "Gasfitería" to 4, "Electricidad" to 5, "Cuidado de mascotas" to 6,
    "Carpintero" to 7, "Pintor" to 8, "Limpieza" to 9,
    "Seguridad" to 10, "Tecnología" to 11
)

// Obtiene la ubicación usando suspendCancellableCoroutine
// (no requiere kotlinx-coroutines-play-services)
@SuppressLint("MissingPermission")
private suspend fun obtenerUbicacion(
    context: android.content.Context,
    onResult: (lat: Double?, lon: Double?, msg: String) -> Unit
) {
    try {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val tokenSource = CancellationTokenSource()

        val location = suspendCancellableCoroutine { cont ->
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
                .addOnSuccessListener { loc -> cont.resume(loc) }
                .addOnFailureListener { cont.resume(null) }
            cont.invokeOnCancellation { tokenSource.cancel() }
        }

        if (location != null) {
            onResult(location.latitude, location.longitude, "📍 Ubicación capturada correctamente")
        } else {
            // Intentar última ubicación conocida
            val lastLocation = suspendCancellableCoroutine { cont ->
                fusedClient.lastLocation
                    .addOnSuccessListener { loc -> cont.resume(loc) }
                    .addOnFailureListener { cont.resume(null) }
            }
            if (lastLocation != null) {
                onResult(lastLocation.latitude, lastLocation.longitude, "📍 Usando última ubicación conocida")
            } else {
                onResult(null, null, "⚠️ Sin señal GPS. Publica igual o activa el GPS e intenta de nuevo.")
            }
        }
    } catch (e: SecurityException) {
        onResult(null, null, "⚠️ Permiso de ubicación denegado.")
    } catch (e: Exception) {
        onResult(null, null, "⚠️ No se pudo obtener ubicación.")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPublicar(navController: NavController, userRepo: UserRepository) {
    val usuarioActual = userRepo.obtenerUsuarioActual()
    val context       = LocalContext.current
    val scope         = rememberCoroutineScope()

    var titulo                by remember { mutableStateOf("") }
    var descripcion           by remember { mutableStateOf("") }
    var precio                by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf(mapaCategorias.keys.first()) }
    var comunaSeleccionada    by remember { mutableStateOf("Maipú") }
    var expandedCategoria     by remember { mutableStateOf(false) }
    var expandedComuna        by remember { mutableStateOf(false) }

    var latitud      by remember { mutableStateOf<Double?>(null) }
    var longitud     by remember { mutableStateOf<Double?>(null) }
    var estadoGps    by remember { mutableStateOf("") }
    var cargandoGps  by remember { mutableStateOf(false) }
    var errorMsg     by remember { mutableStateOf<String?>(null) }
    var cargando     by remember { mutableStateOf(false) }

    val comunas    = listOf("Maipú","La Florida","Santiago Centro","Providencia","Las Condes","Ñuñoa","Puente Alto")
    val categorias = mapaCategorias.keys.toList()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val concedido = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (concedido) {
            scope.launch {
                cargandoGps = true
                obtenerUbicacion(context) { lat, lon, msg ->
                    latitud = lat; longitud = lon; estadoGps = msg; cargandoGps = false
                }
            }
        } else {
            estadoGps = "⚠️ Permiso denegado. Puedes publicar igual sin ubicación."
        }
    }

    LaunchedEffect(Unit) {
        val tienePermiso = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (tienePermiso) {
            cargandoGps = true
            estadoGps = "📍 Obteniendo ubicación..."
            obtenerUbicacion(context) { lat, lon, msg ->
                latitud = lat; longitud = lon; estadoGps = msg; cargandoGps = false
            }
        } else {
            permissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Publicar Trabajo", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(value = titulo, onValueChange = { titulo = it },
            label = { Text("Título") }, modifier = Modifier.fillMaxWidth())

        OutlinedTextField(value = descripcion, onValueChange = { descripcion = it },
            label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

        OutlinedTextField(value = precio, onValueChange = { precio = it },
            label = { Text("Precio (\$)") }, modifier = Modifier.fillMaxWidth())

        // Selector Categoría
        ExposedDropdownMenuBox(
            expanded = expandedCategoria,
            onExpandedChange = { expandedCategoria = !expandedCategoria }
        ) {
            OutlinedTextField(
                value = categoriaSeleccionada, onValueChange = {}, readOnly = true,
                label = { Text("Categoría") },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandedCategoria,
                onDismissRequest = { expandedCategoria = false }) {
                categorias.forEach { op ->
                    DropdownMenuItem(text = { Text(op) },
                        onClick = { categoriaSeleccionada = op; expandedCategoria = false })
                }
            }
        }

        // Selector Comuna
        ExposedDropdownMenuBox(
            expanded = expandedComuna,
            onExpandedChange = { expandedComuna = !expandedComuna }
        ) {
            OutlinedTextField(
                value = comunaSeleccionada, onValueChange = {}, readOnly = true,
                label = { Text("Comuna") },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandedComuna,
                onDismissRequest = { expandedComuna = false }) {
                comunas.forEach { op ->
                    DropdownMenuItem(text = { Text(op) },
                        onClick = { comunaSeleccionada = op; expandedComuna = false })
                }
            }
        }

        // Indicador GPS
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (latitud != null)
                    MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (cargandoGps) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                } else {
                    Icon(Icons.Default.LocationOn, contentDescription = null,
                        tint = if (latitud != null) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(
                        if (estadoGps.isNotBlank()) estadoGps else "Esperando ubicación...",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (latitud != null && longitud != null) {
                        Text(
                            "Lat: ${"%.4f".format(latitud)} | Lon: ${"%.4f".format(longitud)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (estadoGps.contains("⚠️") || estadoGps.contains("sin señal")) {
            OutlinedButton(
                onClick = {
                    cargandoGps = true
                    estadoGps = "📍 Reintentando..."
                    scope.launch {
                        obtenerUbicacion(context) { lat, lon, msg ->
                            latitud = lat; longitud = lon; estadoGps = msg; cargandoGps = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Reintentar obtener ubicación") }
        }

        errorMsg?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(
            onClick = {
                when {
                    usuarioActual == null -> errorMsg = "Debes iniciar sesión para publicar"
                    titulo.isBlank() || descripcion.isBlank() || precio.isBlank() ->
                        errorMsg = "Todos los campos son obligatorios"
                    precio.toDoubleOrNull() == null || precio.toDouble() <= 0 ->
                        errorMsg = "El precio debe ser un número mayor a 0"
                    titulo.length < 5 -> errorMsg = "El título debe ser más descriptivo"
                    else -> {
                        scope.launch {
                            cargando = true
                            try {
                                val nuevoTrabajo = Trabajo(
                                    titulo = titulo,
                                    descripcion = descripcion,
                                    comuna = comunaSeleccionada,
                                    precio = precio.toDouble(),
                                    estado = "Disponible",
                                    latitud = latitud,
                                    longitud = longitud
                                )
                                val response = ApiClient.servicioTrabajos.postTrabajo(
                                    nuevoTrabajo,
                                    (usuarioActual.idUsuario ?: 0).toLong(),
                                    (mapaCategorias[categoriaSeleccionada] ?: 1).toLong()
                                )
                                if (response.isSuccessful) {
                                    navController.navigate("inicio") {
                                        popUpTo("publicar") { inclusive = true }
                                    }
                                } else {
                                    errorMsg = "Error al publicar: ${response.code()}"
                                }
                            } catch (e: Exception) {
                                errorMsg = "Error de conexión: ${e.message}"
                            } finally {
                                cargando = false
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        ) {
            if (cargando) CircularProgressIndicator(modifier = Modifier.size(20.dp))
            else Text("Publicar")
        }
    }
}