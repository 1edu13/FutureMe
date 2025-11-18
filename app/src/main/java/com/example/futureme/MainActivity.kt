package com.example.futureme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.futureme.ui.auth.LoginScreen
import com.example.futureme.ui.theme.FUTUREMETheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = Firebase.auth

        setContent {
            FUTUREMETheme {
                // Esta es la lógica de navegación principal.
                // Comprueba si hay un usuario logueado al iniciar la app.
                if (auth.currentUser == null) {
                    // Si no hay nadie, muestra la pantalla de login.
                    LoginScreen()
                } else {
                    // Si ya hay alguien, muestra la pantalla principal de la app.
                    HomeScreen()
                }
            }
        }
    }
}

/**
 * Pantalla principal de la aplicación que ve el usuario al iniciar sesión.
 */
@Composable
fun HomeScreen() {
    Scaffold(
        // Añadimos el botón flotante para crear nuevas cápsulas.
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Navegar a la pantalla de crear cápsula */ }) {
                Icon(Icons.Default.Add, contentDescription = "Crear nueva cápsula")
            }
        }
    ) { innerPadding ->
        // En el centro, mostraremos la lista de cápsulas del usuario.
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Aquí irá la lista de cápsulas")
        }
    }
}

@Preview(showBackground = true, name = "Home Screen Preview")
@Composable
fun HomeScreenPreview() {
    FUTUREMETheme {
        HomeScreen()
    }
}
