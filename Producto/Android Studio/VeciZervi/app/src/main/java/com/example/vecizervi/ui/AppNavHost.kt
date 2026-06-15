package com.example.vecizervi.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.vecizervi.data.repositories.UserRepository
import com.example.vecizervi.data.repositories.TrabajoRepository
import com.example.vecizervi.ui.components.BottomBar

private val rutasSinBottomBar = listOf("login", "register", "recuperar")

@Composable
fun AppNavHost(
    navController: NavHostController,
    trabajoRepo: TrabajoRepository,
    userRepo: UserRepository
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route
    val mostrarBottomBar = rutaActual != null && rutasSinBottomBar.none { rutaActual.startsWith(it) }

    Scaffold(
        bottomBar = {
            if (mostrarBottomBar) {
                BottomBar(navController, userRepo.obtenerUsuarioActual()?.rol ?: "USER")
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login")       { PantallaLogin(navController, userRepo) }
            composable("register")    { PantallaRegister(navController, userRepo) }
            composable("recuperar")   { PantallaRecuperarPassword(navController, userRepo) }
            composable("inicio")      { PantallaInicio(navController, userRepo) }
            composable("publicar")    { PantallaPublicar(navController, userRepo) }
            composable("perfil")      { PantallaPerfil(navController, userRepo) }
            composable("editarPerfil"){ PantallaEditarPerfil(navController, userRepo) }
            composable("chat")        { PantallaChatInbox(navController, userRepo) }
            composable("admin")       { PantallaAdmin(navController, userRepo) }

            composable("editar_usuario/{idUsuario}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("idUsuario")?.toIntOrNull() ?: return@composable
                PantallaEditarUsuario(navController, id)
            }

            composable("editar_trabajo/{trabajoId}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("trabajoId")?.toLongOrNull() ?: return@composable
                PantallaEditarTrabajo(navController, id, trabajoRepo, userRepo)
            }

            composable("detalleTrabajo/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: -1L
                PantallaDetalleTrabajo(navController, id, userRepo)
            }

            composable("chat/{autorNombre}/{idTrabajo}/{idOtroUsuario}") { backStackEntry ->
                val autorNombre    = backStackEntry.arguments?.getString("autorNombre")
                val idTrabajo      = backStackEntry.arguments?.getString("idTrabajo")?.toIntOrNull() ?: -1
                val idOtroUsuario  = backStackEntry.arguments?.getString("idOtroUsuario")?.toIntOrNull() ?: 0
                PantallaChat(navController, userRepo, autorNombre, idTrabajo, idOtroUsuario)
            }

            // Ruta legada sin idOtroUsuario (por si hay navegación antigua)
            composable("chat/{autorNombre}/{idTrabajo}") { backStackEntry ->
                val autorNombre = backStackEntry.arguments?.getString("autorNombre")
                val idTrabajo   = backStackEntry.arguments?.getString("idTrabajo")?.toIntOrNull() ?: -1
                PantallaChat(navController, userRepo, autorNombre, idTrabajo, 0)
            }

            composable("editar/{trabajoId}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("trabajoId")?.toLongOrNull()
                if (id != null) PantallaEditarTrabajo(navController, id, trabajoRepo, userRepo)
            }
        }
    }
}