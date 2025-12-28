package com.example.futureme.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futureme.R
import com.example.futureme.ui.theme.FutureMeTheme
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState



enum class AuthMode {
    LOGIN,
    SIGN_UP
}

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

    // 🎨 Paleta (texto blanco roto + dorado acento)
    val gold = Color(0xFFD4AF37)

    val textPrimary = Color(0xFFEDEDED)                // 👈 blanco roto principal
    val textSecondary = textPrimary.copy(alpha = 0.78f)
    val hint = textPrimary.copy(alpha = 0.55f)

    val goldBorder = gold.copy(alpha = 0.55f)
    val goldAccent = gold.copy(alpha = 0.85f)

    Box(modifier = Modifier.fillMaxSize()) {

        // 🔹 Fondo imagen
        Image(
            painter = painterResource(id = R.drawable.login_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 🔹 Overlay oscuro (para legibilidad)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.55f),
                        0.5f to Color.Black.copy(alpha = 0.35f),
                        1f to Color.Black.copy(alpha = 0.62f)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = if (authMode == AuthMode.LOGIN) "Bienvenido de Nuevo" else "Crea tu Cuenta",
                color = textPrimary, // 👈 antes goldText
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(30.dp))

            if (authMode == AuthMode.SIGN_UP) {
                GoldTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Nombre",
                    enabled = !isLoading,
                    textColor = textPrimary,
                    borderColor = goldBorder,
                    hintColor = hint,
                    cursorColor = goldAccent
                )
                Spacer(modifier = Modifier.height(18.dp))
            }

            GoldTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email",
                keyboardType = KeyboardType.Email,
                enabled = !isLoading,
                textColor = textPrimary,
                borderColor = goldBorder,
                hintColor = hint,
                cursorColor = goldAccent
            )

            Spacer(modifier = Modifier.height(18.dp))

            GoldTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Contraseña",
                isPassword = true,
                enabled = !isLoading,
                textColor = textPrimary,
                borderColor = goldBorder,
                hintColor = hint,
                cursorColor = goldAccent
            )

            error?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(if (error == null) 34.dp else 26.dp))

            if (isLoading) {
                CircularProgressIndicator(color = goldAccent)
            } else {

                GoldButton(
                    text = if (authMode == AuthMode.LOGIN) "Iniciar Sesión" else "Registrarse",
                    onClick = {
                        if (authMode == AuthMode.LOGIN) {
                            onSignIn(email, password)
                        } else {
                            onSignUp(name, email, password)
                        }
                    },
                    gold = gold,
                    textPrimary = textPrimary
                )

                Spacer(modifier = Modifier.height(18.dp))

                val (question, action) = if (authMode == AuthMode.LOGIN) {
                    "¿No tienes cuenta?" to "Regístrate"
                } else {
                    "¿Ya tienes cuenta?" to "Inicia Sesión"
                }

                Row {
                    Text(question, color = textSecondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = action,
                        color = goldAccent, // 👈 acento dorado solo aquí
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {
                            authMode = if (authMode == AuthMode.LOGIN) AuthMode.SIGN_UP else AuthMode.LOGIN
                        }
                    )
                }
            }
        }
    }
}


/* ---------- COMPONENTES ---------- */

@Composable
private fun GoldTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
    textColor: Color,
    borderColor: Color,
    hintColor: Color,
    cursorColor: Color,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        placeholder = { Text(placeholder, color = hintColor) },
        textStyle = LocalTextStyle.current.copy(color = textColor, fontSize = 16.sp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = borderColor.copy(alpha = 0.85f),
            unfocusedBorderColor = borderColor,
            focusedContainerColor = Color.Black.copy(alpha = 0.35f),
            unfocusedContainerColor = Color.Black.copy(alpha = 0.30f),
            cursorColor = cursorColor,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor
        )
    )
}

@Composable
private fun GoldButton(
    text: String,
    onClick: () -> Unit,
    gold: Color,
    textPrimary: Color
) {
    val shape = RoundedCornerShape(28.dp)

    val borderBase = gold.copy(alpha = 0.65f)
    val glow = gold.copy(alpha = 0.30f)

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val containerColor by animateColorAsState(
        targetValue = if (pressed) gold.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.55f),
        label = "buttonContainer"
    )
    val borderColor by animateColorAsState(
        targetValue = if (pressed) gold.copy(alpha = 0.90f) else borderBase,
        label = "buttonBorder"
    )
    val elevation by animateDpAsState(
        targetValue = if (pressed) 10.dp else 18.dp,
        label = "buttonElevation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = glow,
                spotColor = glow
            )
    ) {
        Button(
            onClick = onClick,
            shape = shape,
            modifier = Modifier.fillMaxSize(),
            interactionSource = interactionSource,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = textPrimary
            ),
            border = BorderStroke(1.dp, borderColor)
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = textPrimary // 👈 blanco roto
            )
        }
    }
}



/* ---------- PREVIEWS ---------- */

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    FutureMeTheme {
        LoginScreenContent(
            isLoading = false,
            error = null,
            onSignIn = { _, _ -> },
            onSignUp = { _, _, _ -> }
        )
    }
}
