package com.example.vecizervi.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@Composable
fun BottomBar(navController: NavController, rol: String = "USER") {
    val itemsBase = listOf(
        BottomNavItem("inicio", Icons.Default.Home, "Inicio"),
        BottomNavItem("chat", Icons.Default.Chat, "Chat"),
        BottomNavItem("publicar", Icons.Default.Add, "Publicar"),
        BottomNavItem("perfil", Icons.Default.Person, "Perfil")
    )

    // Si es admin agrega el botón de administración
    val items = if (rol == "ADMIN") {
        itemsBase + BottomNavItem("admin", Icons.Default.AdminPanelSettings, "Admin")
    } else {
        itemsBase
    }

    NavigationBar {
        val navBackStackEntry = navController.currentBackStackEntryAsState().value
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}