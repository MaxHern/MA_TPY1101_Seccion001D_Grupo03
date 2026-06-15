package com.example.vecizervi.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.vecizervi.data.models.Trabajo
import com.example.vecizervi.data.repositories.UserRepository
import com.example.vecizervi.data.utils.ApiClient
import kotlinx.coroutines.launch
import kotlin.math.*

private fun calcularDistanciaKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    return r * 2 * atan2(sqrt(a), sqrt(1 - a))
}

private fun formatearDistancia(km: Double): String =
    if (km < 1.0) "${(km * 1000).toInt()} m" else "${"%.1f".format(km)} km"

private fun esCoordenadaEnChile(lat: Double?, lon: Double?): Boolean {
    if (lat == null || lon == null || lat == 0.0 || lon == 0.0) return false
    return lat in -56.0..-17.0 && lon in -75.0..-66.0
}

private const val LAT_REF = -33.4489
private const val LON_REF = -70.6693

private val COORDENADAS_COMUNAS = mapOf(
    "Maipú"            to Pair(-33.5122, -70.7653),
    "La Florida"       to Pair(-33.5167, -70.5833),
    "Santiago Centro"  to Pair(-33.4489, -70.6693),
    "Santiago"         to Pair(-33.4489, -70.6693),
    "Providencia"      to Pair(-33.4333, -70.6167),
    "Las Condes"       to Pair(-33.4167, -70.5833),
    "Ñuñoa"            to Pair(-33.4500, -70.6000),
    "Puente Alto"      to Pair(-33.6000, -70.5833),
    "Cerrillos"        to Pair(-33.4956, -70.7272),
    "Estación Central" to Pair(-33.4558, -70.6817),
    "Quilicura"        to Pair(-33.3617, -70.7408),
    "Recoleta"         to Pair(-33.4094, -70.6397),
    "Independencia"    to Pair(-33.4183, -70.6572),
    "San Miguel"       to Pair(-33.4981, -70.6478),
    "La Cisterna"      to Pair(-33.5258, -70.6619),
    "El Bosque"        to Pair(-33.5622, -70.6711),
    "San Bernardo"     to Pair(-33.5928, -70.6992),
    "Peñalolén"        to Pair(-33.4833, -70.5333),
    "Macul"            to Pair(-33.4858, -70.5978),
    "Lo Espejo"        to Pair(-33.5175, -70.6928),
    "Pudahuel"         to Pair(-33.4417, -70.7633)
)

private val CATEGORIAS = listOf(
    "Todas", "Fletes", "Aseo doméstico", "Jardinería",
    "Gasfitería", "Electricidad", "Cuidado de mascotas",
    "Carpintero", "Pintor", "Limpieza", "Seguridad", "Tecnología"
)

private val COMUNAS_FILTRO = listOf(
    "Todas", "Maipú", "La Florida", "Santiago Centro", "Santiago",
    "Providencia", "Las Condes", "Ñuñoa", "Puente Alto",
    "Cerrillos", "Estación Central", "San Miguel", "Peñalolén",
    "Pudahuel", "Quilicura", "San Bernardo"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicio(navController: NavController, userRepo: UserRepository) {
    val context = LocalContext.current

    // FIX-12: verificar permiso GPS al montar la pantalla
    var locationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> locationGranted = isGranted }

    LaunchedEffect(Unit) {
        if (!locationGranted) permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    var searchText            by remember { mutableStateOf("") }
    var selectedComuna        by remember { mutableStateOf("Todas") }
    var selectedCategoria     by remember { mutableStateOf("Todas") }
    var comunaMenuExpanded    by remember { mutableStateOf(false) }
    var categoriaMenuExpanded by remember { mutableStateOf(false) }
    var kmMaximo              by remember { mutableStateOf(50f) }

    var trabajos by remember { mutableStateOf<List<Trabajo>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope    = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    LaunchedEffect(rutaActual) {
        if (rutaActual == "inicio") {
            cargando = true
            errorMsg = null
            try {
                val response = ApiClient.servicioTrabajos.getTrabajosDisponibles()
                if (response.isSuccessful) trabajos = response.body() ?: emptyList()
                else errorMsg = "Error ${response.code()}"
            } catch (e: Exception) {
                errorMsg = "No se pudo conectar al servidor"
            } finally { cargando = false }
        }
    }

    val trabajosFiltrados = trabajos
        .filter { it.estado == "Disponible" }
        .filter {
            searchText.isBlank() ||
                    it.titulo.contains(searchText, ignoreCase = true) ||
                    it.descripcion.contains(searchText, ignoreCase = true)
        }
        .filter { selectedComuna == "Todas" || it.comuna.equals(selectedComuna, ignoreCase = true) }
        .filter {
            selectedCategoria == "Todas" ||
                    it.categoria?.nombreCategoria?.equals(selectedCategoria, ignoreCase = true) == true
        }
        .filter { trabajo ->
            val gpsValido = esCoordenadaEnChile(trabajo.latitud, trabajo.longitud)
            when {
                gpsValido ->
                    calcularDistanciaKm(LAT_REF, LON_REF, trabajo.latitud!!, trabajo.longitud!!) <= kmMaximo
                COORDENADAS_COMUNAS.containsKey(trabajo.comuna) -> {
                    val c = COORDENADAS_COMUNAS[trabajo.comuna]!!
                    calcularDistanciaKm(LAT_REF, LON_REF, c.first, c.second) <= kmMaximo
                }
                else -> true
            }
        }
        .sortedBy { trabajo ->
            val gpsValido = esCoordenadaEnChile(trabajo.latitud, trabajo.longitud)
            when {
                gpsValido -> calcularDistanciaKm(LAT_REF, LON_REF, trabajo.latitud!!, trabajo.longitud!!)
                COORDENADAS_COMUNAS.containsKey(trabajo.comuna) -> {
                    val c = COORDENADAS_COMUNAS[trabajo.comuna]!!
                    calcularDistanciaKm(LAT_REF, LON_REF, c.first, c.second)
                }
                else -> Double.MAX_VALUE
            }
        }

    Scaffold(topBar = { TopAppBar(title = { Text("Trabajos disponibles") }) }) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {

            OutlinedTextField(
                value = searchText, onValueChange = { searchText = it },
                placeholder = { Text("Busca Trabajos Cercanos") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = MaterialTheme.shapes.large
            )
            Spacer(Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(onClick = { categoriaMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (selectedCategoria == "Todas") "Categoría ▾" else "$selectedCategoria ▾", maxLines = 1)
                    }
                    DropdownMenu(expanded = categoriaMenuExpanded, onDismissRequest = { categoriaMenuExpanded = false }) {
                        CATEGORIAS.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { selectedCategoria = cat; categoriaMenuExpanded = false })
                        }
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(onClick = { comunaMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (selectedComuna == "Todas") "Comuna ▾" else "$selectedComuna ▾", maxLines = 1)
                    }
                    DropdownMenu(expanded = comunaMenuExpanded, onDismissRequest = { comunaMenuExpanded = false }) {
                        COMUNAS_FILTRO.forEach { comuna ->
                            DropdownMenuItem(text = { Text(comuna) }, onClick = { selectedComuna = comuna; comunaMenuExpanded = false })
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Distancia máxima:", style = MaterialTheme.typography.labelMedium)
                Text("${kmMaximo.toInt()} km", style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Slider(value = kmMaximo, onValueChange = { kmMaximo = it },
                valueRange = 1f..50f, steps = 48, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(4.dp))

            // FIX-12: banner cuando el permiso GPS no está concedido
            if (!locationGranted) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        "Activa tu GPS para ver la distancia exacta a cada trabajo",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            if (!cargando && errorMsg == null) {
                Text("${trabajosFiltrados.size} de ${trabajos.size} trabajo(s)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
            }

            when {
                cargando -> Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                    CircularProgressIndicator()
                }
                errorMsg != null -> Text(errorMsg!!, color = MaterialTheme.colorScheme.error)
                trabajosFiltrados.isEmpty() && trabajos.isNotEmpty() -> Box(
                    Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No hay trabajos con estos filtros",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center)
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = {
                            selectedCategoria = "Todas"
                            selectedComuna = "Todas"
                            kmMaximo = 50f
                            searchText = ""
                        }) { Text("Limpiar filtros") }
                    }
                }
                trabajos.isEmpty() -> Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                    Text("No hay trabajos disponibles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(trabajosFiltrados) { trabajo ->
                            val gpsValido = esCoordenadaEnChile(trabajo.latitud, trabajo.longitud)
                            val distanciaKm = when {
                                gpsValido -> calcularDistanciaKm(LAT_REF, LON_REF, trabajo.latitud!!, trabajo.longitud!!)
                                COORDENADAS_COMUNAS.containsKey(trabajo.comuna) -> {
                                    val c = COORDENADAS_COMUNAS[trabajo.comuna]!!
                                    calcularDistanciaKm(LAT_REF, LON_REF, c.first, c.second)
                                }
                                else -> null
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable { navController.navigate("detalleTrabajo/${trabajo.idTrabajo}") },
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(trabajo.titulo, style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold)
                                            trabajo.cliente?.let {
                                                Text("° ${it.nombres} ${it.apellidos}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                        Text("$${"%,.0f".format(trabajo.precio)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(trabajo.descripcion, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                                    Spacer(Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically) {
                                        trabajo.categoria?.nombreCategoria?.let { cat ->
                                            Surface(shape = MaterialTheme.shapes.small,
                                                color = MaterialTheme.colorScheme.secondaryContainer) {
                                                Text(cat, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer)
                                            }
                                        }
                                        if (distanciaKm != null) {
                                            Surface(shape = MaterialTheme.shapes.small,
                                                color = MaterialTheme.colorScheme.tertiaryContainer) {
                                                Text("A ${formatearDistancia(distanciaKm)}",
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onTertiaryContainer)
                                            }
                                        } else {
                                            // FIX-16: texto ya correcto
                                            Surface(shape = MaterialTheme.shapes.small,
                                                color = MaterialTheme.colorScheme.surfaceVariant) {
                                                Text("Ubicación exacta no especificada",
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                        Spacer(Modifier.weight(1f))
                                        Button(
                                            onClick = { navController.navigate("detalleTrabajo/${trabajo.idTrabajo}") },
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                                        ) { Text("Ver Más", style = MaterialTheme.typography.labelMedium) }
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
