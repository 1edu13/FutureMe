package com.example.futureme.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource // ✅ Import necesario
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futureme.R // ✅ Import necesario
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
            // ✅ CORREGIDO: Usar recursos para título dinámico
            val titleText = if (authMode == AuthMode.LOGIN) {
                stringResource(R.string.login_welcome)
            } else {
                stringResource(R.string.login_create_account)
            }

            TitleBlock(
                title = titleText,
                isDark = isDark
            )

            Spacer(Modifier.height(42.dp))

            if (authMode == AuthMode.SIGN_UP) {
                AppTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = stringResource(R.string.placeholder_name), // ✅ CORREGIDO
                    enabled = !isLoading,
                    isDark = isDark
                )
                Spacer(Modifier.height(14.dp))
            }

            AppTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = stringResource(R.string.placeholder_email), // ✅ CORREGIDO
                keyboardType = KeyboardType.Email,
                enabled = !isLoading,
                isDark = isDark
            )

            Spacer(Modifier.height(14.dp))

            AppTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = stringResource(R.string.placeholder_password), // ✅ CORREGIDO
                enabled = !isLoading,
                isDark = isDark,
                isPassword = true
            )

            if (!error.isNullOrBlank()) {
                Spacer(Modifier.height(14.dp))
                // Nota: 'error' viene del ViewModel. Si es un mensaje de Firebase (ej: "Bad password"), vendrá en inglés/idioma del sistema.
                // Si quieres traducirlo, deberías mapear códigos de error en el ViewModel.
                Text(text = error, color = Color(0xFFEF5350), fontSize = 14.sp)
            }

            Spacer(Modifier.height(36.dp))

            if (isLoading) {
                CircularProgressIndicator(color = OroAmbar)
            } else {
                // ✅ CORREGIDO: Botón principal dinámico
                val btnText = if (authMode == AuthMode.LOGIN) {
                    stringResource(R.string.btn_login)
                } else {
                    stringResource(R.string.btn_register)
                }

                AppButton(
                    text = btnText,
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
    // ✅ CORREGIDO: Textos de alternancia (switch login/signup)
    val questionText = if (authMode == AuthMode.LOGIN) {
        stringResource(R.string.toggle_no_account)
    } else {
        stringResource(R.string.toggle_has_account)
    }

    val actionText = if (authMode == AuthMode.LOGIN) {
        stringResource(R.string.action_register)
    } else {
        stringResource(R.string.action_login)
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = questionText,
            color = GrisAzulado
        )
        TextButton(onClick = onToggle, contentPadding = PaddingValues(0.dp)) {
            Text(
                text = actionText,
                color = OroAmbar,
                fontWeight = FontWeight.Bold
            )
        }
    }
}