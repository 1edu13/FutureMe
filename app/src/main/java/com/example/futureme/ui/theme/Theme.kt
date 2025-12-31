package com.example.futureme.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ─────────────────────────────
// 🌙 DARK COLOR SCHEME
// ─────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary = OroAmbar,
    secondary = Color(0xFFFFD54F),
    tertiary = Color(0xFFFFECB3),

    background = AzulProfundo,
    surface = AzulSuperficie,

    onPrimary = AzulProfundo,
    onSecondary = AzulProfundo,
    onTertiary = AzulProfundo,

    onBackground = BlancoAzulado,
    onSurface = BlancoAzulado
)

// ─────────────────────────────
// ☀️ LIGHT COLOR SCHEME
// ─────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary = OroAmbar,
    onPrimary = Color.White,
    background = ClaroBase,
    surface = ClaroSuave,           // Las tarjetas usarán el color Arena
    onBackground = TextoCafe,       // El texto será Café oscuro
    onSurface = TextoCafe,
    outline = ClaroBorde            // Bordes nítidos
)

@Composable
fun FutureMeTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean = false, // ❌ mejor desactivado para tu diseño
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
