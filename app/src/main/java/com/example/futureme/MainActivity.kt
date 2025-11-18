package com.example.futureme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.futureme.ui.auth.AuthViewModel
import com.example.futureme.ui.auth.LoginScreen
import com.example.futureme.ui.capsule.CapsuleViewModel
import com.example.futureme.ui.capsule.CreateCapsuleScreen
import com.example.futureme.ui.navigation.Screen
import com.example.futureme.ui.theme.FUTUREMETheme

class MainActivity : ComponentActivity() {

    // Obtenemos las instancias de nuestros ViewModels
    private val authViewModel: AuthViewModel by viewModels()
    private val capsuleViewModel: CapsuleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FUTUREMETheme {
                // Pasamos ambos ViewModels al NavHost
                AppNavHost(authViewModel = authViewModel, capsuleViewModel = capsuleViewModel)
            }
        }
    }
}

@Composable
fun AppNavHost(
    authViewModel: AuthViewModel, 
    capsuleViewModel: CapsuleViewModel
) {
    val navController = rememberNavController()
    val user by authViewModel.user.collectAsState()

    LaunchedEffect(user) {
        if (user != null) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        } else {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(authViewModel = authViewModel)
        }
        composable(Screen.Home.route) {
            HomeScreen(
                authViewModel = authViewModel,
                onNavigateToCreate = { navController.navigate(Screen.CreateCapsule.route) }
            )
        }
        composable(Screen.CreateCapsule.route) {
            // ¡Aquí está la corrección!
            CreateCapsuleScreen(
                capsuleViewModel = capsuleViewModel, 
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    onNavigateToCreate: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Cápsulas") },
                actions = {
                    IconButton(onClick = { authViewModel.signOut() }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreate) {
                Icon(Icons.Default.Add, contentDescription = "Crear nueva cápsula")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Aquí irá la lista de cápsulas")
        }
    }
}
