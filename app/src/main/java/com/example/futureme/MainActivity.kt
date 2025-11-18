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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.futureme.ui.auth.AuthViewModel
import com.example.futureme.ui.auth.LoginScreen
import com.example.futureme.ui.theme.FUTUREMETheme

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FUTUREMETheme {
                val user by authViewModel.user.collectAsState()

                if (user == null) {
                    LoginScreen(authViewModel = authViewModel)
                } else {
                    HomeScreen(authViewModel = authViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(authViewModel: AuthViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Cápsulas") },
                actions = {
                    // Botón para cerrar sesión
                    IconButton(onClick = { authViewModel.signOut() }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Navegar a la pantalla de crear cápsula */ }) {
                Icon(Icons.Default.Add, contentDescription = "Crear nueva cápsula")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Aquí irá la lista de cápsulas")
        }
    }
}

@Preview(showBackground = true, name = "Home Screen Preview")
@Composable
fun HomeScreenPreview() {
    FUTUREMETheme {
        HomeScreen(authViewModel = AuthViewModel())
    }
}
