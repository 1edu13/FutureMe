package com.example.futureme.ui.components

import androidx.compose.runtime.Composable

@Composable
fun AppButton(text: String, onClick: () -> Unit) {
    NightButton(text = text, onClick = onClick)
}
