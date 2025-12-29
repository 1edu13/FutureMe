package com.example.futureme.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futureme.ui.components.AppButton
import com.example.futureme.ui.components.AppTextField
import com.example.futureme.ui.theme.AppBackground
import com.example.futureme.ui.theme.BackgroundType
import com.example.futureme.ui.theme.OroAmbar
import com.example.futureme.ui.theme.BlancoAzulado
import com.example.futureme.ui.theme.GrisAzulado
import com.example.futureme.ui.theme.AzulProfundo

enum class AuthMode { LOGIN, SIGN_UP }

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    isDark: Boolean // <- pásalo desde ThemeViewModel
) {
    val isLoading by authViewModel.isLoading.collectAsState()
    val error by authViewModel.error.collectAsState()

    LoginScreenContent(
        isDark = isDark,
        isLoading = isLoading,
        error = error,
        onSignIn = { email, password -> authViewModel.signIn(email, password) },
        onSignUp = { name, email, password -> authViewModel.signUp(name, email, password) }
    )
}

@Composable
fun LoginScreenContent(
    isDark: Boolean,
    isLoading: Boolean,
    error: String?,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }

    AppBackground(type = BackgroundType.LOGIN_IMAGE, isDark = isDark) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TitleBlock(
                title = if (authMode == AuthMode.LOGIN) "Bienvenido de Nuevo" else "Crea tu Cuenta",
                isDark = isDark
            )

            Spacer(Modifier.height(42.dp))

            if (authMode == AuthMode.SIGN_UP) {
                AppTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Nombre",
                    enabled = !isLoading,
                    isDark = isDark
                )
                Spacer(Modifier.height(14.dp))
            }

            AppTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email",
                keyboardType = KeyboardType.Email,
                enabled = !isLoading,
                isDark = isDark
            )

            Spacer(Modifier.height(14.dp))

            AppTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Contraseña",
                enabled = !isLoading,
                isDark = isDark,
                isPassword = true
            )

            if (!error.isNullOrBlank()) {
                Spacer(Modifier.height(14.dp))
                Text(text = error, color = Color(0xFFEF5350), fontSize = 14.sp)
            }

            Spacer(Modifier.height(36.dp))

            if (isLoading) {
                CircularProgressIndicator(color = OroAmbar)
            } else {
                AppButton(
                    text = if (authMode == AuthMode.LOGIN) "Iniciar Sesión" else "Registrarse",
                    onClick = {
                        if (authMode == AuthMode.LOGIN) onSignIn(email, password)
                        else onSignUp(name, email, password)
                    }
                )

                Spacer(Modifier.height(18.dp))

                AuthModeToggle(
                    authMode = authMode,
                    onToggle = { authMode = if (authMode == AuthMode.LOGIN) AuthMode.SIGN_UP else AuthMode.LOGIN }
                )
            }
        }
    }
}

@Composable
private fun TitleBlock(title: String, isDark: Boolean) {
    val titleColor = if (isDark) BlancoAzulado else AzulProfundo

    Text(
        text = title,
        color = titleColor,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun AuthModeToggle(authMode: AuthMode, onToggle: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = if (authMode == AuthMode.LOGIN) "¿No tienes cuenta? " else "¿Ya tienes cuenta? ",
            color = GrisAzulado
        )
        TextButton(onClick = onToggle, contentPadding = PaddingValues(0.dp)) {
            Text(
                text = if (authMode == AuthMode.LOGIN) "Regístrate" else "Inicia Sesión",
                color = OroAmbar,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


