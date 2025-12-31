package com.example.futureme.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futureme.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDark: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onShowTutorialAgain: () -> Unit,
    onLanguageClick: (() -> Unit)? = null
) {
    // 🎨 USANDO TU PALETA DE COLOR.KT
    val gold = OroAmbar
    val titleGold = if (isDark) gold else AzulProfundo
    val textPrimary = if (isDark) BlancoAzulado else AzulProfundo
    val textSecondary = if (isDark) GrisAzulado else ClaroBorde

    // --- EFECTO SEDA/CRISTAL PREMIUM PARA EL CUADRO ---
    val cardBrush = if (isDark) {
        Brush.verticalGradient(listOf(AzulSuperficie, AzulProfundo))
    } else {
        // Combinamos tus colores: Base -> Suave -> Principal para evitar el blanco nuclear
        Brush.verticalGradient(listOf(ClaroBase, ClaroSuave, ClaroPrincipal))
    }

    val cardBorder = if (isDark) gold.copy(alpha = 0.3f) else ClaroBorde.copy(alpha = 0.8f)

    AppBackground(type = BackgroundType.GRADIENT, isDark = isDark) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "AJUSTES",
                            color = gold,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = gold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(12.dp))

                // Cuadro principal de Ajustes con el nuevo diseño
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.5.dp, cardBorder),
                    shadowElevation = if (isDark) 8.dp else 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .background(cardBrush) // Aplicamos el degradado premium
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Apariencia",
                            color = gold,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(Modifier.height(16.dp))

                        ThemeRow(
                            icon = { Icon(Icons.Default.Palette, contentDescription = null, tint = gold) },
                            title = "Modo Oscuro",
                            subtitle = "Cambia el estilo visual de la app",
                            checked = isDark,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onCheckedChange = onToggleTheme
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = gold.copy(alpha = 0.15f)
                        )

                        SettingRow(
                            icon = { Icon(Icons.Default.Language, contentDescription = null, tint = gold) },
                            title = "Idioma",
                            subtitle = "Español (predeterminado)",
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onClick = { onLanguageClick?.invoke() }
                        )

                        Spacer(Modifier.height(24.dp))

                        Text(
                            text = "Ayuda y Soporte",
                            color = gold,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(Modifier.height(16.dp))

                        SettingRow(
                            icon = { Icon(Icons.Default.PlayCircle, contentDescription = null, tint = gold) },
                            title = "¿Qué es FutureMe?",
                            subtitle = "Breve explicación",
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onClick = onShowTutorialAgain
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = gold.copy(alpha = 0.15f)
                        )

                        SettingRow(
                            icon = { Icon(Icons.Default.Info, contentDescription = null, tint = gold) },
                            title = "Versión",
                            subtitle = "1.0.0 (Build 2025)",
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onClick = {}
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    textPrimary: Color,
    textSecondary: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) { icon() }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = textPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = textSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ThemeRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    checked: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) { icon() }
        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = textPrimary,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = textSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = OroAmbar,
                checkedTrackColor = OroAmbar.copy(alpha = 0.5f)
            )
        )
    }
}