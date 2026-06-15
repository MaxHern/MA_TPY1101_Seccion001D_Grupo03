package com.example.vecizervi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.vecizervi.data.repositories.TrabajoRepository
import com.example.vecizervi.data.repositories.UserRepository
import com.example.vecizervi.ui.AppNavHost
import com.example.vecizervi.ui.theme.VeciZerviTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userRepo = UserRepository()
        val trabajoRepo = TrabajoRepository()

        setContent {
            VeciZerviTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    AppNavHost(navController, trabajoRepo, userRepo)
                }
            }
        }
    }
}
