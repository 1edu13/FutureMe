package com.example.futureme.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.futureme.ui.theme.FUTUREMETheme

// Enum para definir el modo de la pantalla
enum class AuthMode {
    LOGIN,
    SIGN_UP
}

/**
 * El Composable "inteligente" que se conecta al ViewModel.
 */
@Composable
fun LoginScreen(authViewModel: AuthViewModel) {
    val isLoading by authViewModel.isLoading.collectAsState()
    val error by authViewModel.error.collectAsState()

    LoginScreenContent(
        isLoading = isLoading,
        error = error,
        onSignIn = { email, password -> authViewModel.signIn(email, password) },
        onSignUp = { name, email, password -> authViewModel.signUp(name, email, password) }
    )
}

/**
 * El Composable "tonto" que solo se encarga de la UI.
 */
@Composable
fun LoginScreenContent(
    isLoading: Boolean,
    error: String?,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (authMode == AuthMode.LOGIN) "Bienvenido de Nuevo" else "Crea tu Cuenta",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (authMode == AuthMode.SIGN_UP) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (authMode == AuthMode.LOGIN) {
                        onSignIn(email, password)
                    } else {
                        onSignUp(name, email, password)
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (authMode == AuthMode.LOGIN) "Iniciar Sesión" else "Registrarse")
            }

            Spacer(modifier = Modifier.height(16.dp))

            val (question, action) = if (authMode == AuthMode.LOGIN) {
                "¿No tienes cuenta?" to "Regístrate"
            } else {
                "¿Ya tienes cuenta?" to "Inicia Sesión"
            }

            Row {
                Text(question)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = action,
                    modifier = Modifier.clickable { 
                        authMode = if (authMode == AuthMode.LOGIN) AuthMode.SIGN_UP else AuthMode.LOGIN
                    },
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Login Screen Preview")
@Composable
fun LoginScreenContentPreview() {
    FUTUREMETheme {
        LoginScreenContent(
            isLoading = false,
            error = null,
            onSignIn = { _, _ -> },
            onSignUp = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "Login Screen Loading Preview")
@Composable
fun LoginScreenContentLoadingPreview() {
    FUTUREMETheme {
        LoginScreenContent(
            isLoading = true, // <-- ¡Ahora podemos previsualizar el estado de carga!
            error = null,
            onSignIn = { _, _ -> },
            onSignUp = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "Login Screen Error Preview")
@Composable
fun LoginScreenContentErrorPreview() {
    FUTUREMETheme {
        LoginScreenContent(
            isLoading = false,
            error = "Email o contraseña incorrectos.", // <-- ¡Y el estado de error!
            onSignIn = { _, _ -> },
            onSignUp = { _, _, _ -> }
        )
    }
}
