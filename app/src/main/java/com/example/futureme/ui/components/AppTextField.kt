package com.example.futureme.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
    isDark: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    NightTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        enabled = enabled,
        isDark = isDark,
        keyboardType = keyboardType,
        isPassword = isPassword
    )
}
