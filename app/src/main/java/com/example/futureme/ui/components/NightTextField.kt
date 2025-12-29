package com.example.futureme.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.futureme.ui.theme.*

@Composable
fun NightTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
    isDark: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    val container = if (isDark) AzulSuperficie.copy(alpha = 0.22f) else ClaroBase.copy(alpha = 0.55f)
    val containerFocused = if (isDark) AzulSuperficie.copy(alpha = 0.40f) else ClaroBase.copy(alpha = 0.72f)
    val border = if (isDark) AzulSuperficie else ClaroBorde.copy(alpha = 0.55f)

    val textColor = if (isDark) BlancoAzulado else AzulProfundo
    val placeholderColor = if (isDark) GrisAzulado.copy(alpha = 0.6f) else AzulProfundo.copy(alpha = 0.55f)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        placeholder = { Text(placeholder, color = placeholderColor) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = OroAmbar,
            unfocusedBorderColor = border,
            focusedContainerColor = containerFocused,
            unfocusedContainerColor = container,
            cursorColor = OroAmbar,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor
        )
    )
}
