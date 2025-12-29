package com.example.futureme.ui.settings

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onShowTutorialAgain: () -> Unit,
    onLanguageClick: (() -> Unit)? = null,
    onThemeClick: (() -> Unit)? = null
) {
    val gold = Color(0xFFD4AF37)
    val titleGold = Color(0xFFC9A84D)
    val textPrimary = Color(0xFFEDEDED)
    val textSecondary = textPrimary.copy(alpha = 0.75f)
    val bg = Color(0xFF0B0B0B)
    val cardBg = Color(0xFF0E0E0E)
    val toolbarBg = Color(0xFF161616)

    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = bg,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Ajustes",
                            color = titleGold,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = gold
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = toolbarBg,
                        titleContentColor = titleGold,
                        navigationIconContentColor = gold,
                        actionIconContentColor = gold
                    )
                )
                Divider(color = gold.copy(alpha = 0.20f), thickness = 1.dp)
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // =========================
            // GENERAL
            // =========================
            SectionCard(
                title = "General",
                titleColor = titleGold,
                cardBg = cardBg,
                gold = gold
            ) {
                SettingRow(
                    icon = { Icon(Icons.Default.Language, contentDescription = null, tint = gold) },
                    title = "Idioma",
                    subtitle = "Pendiente (se añadirá más adelante)",
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    onClick = { onLanguageClick?.invoke() }
                )

                Divider(color = gold.copy(alpha = 0.10f), thickness = 1.dp)

                SettingRow(
                    icon = { Icon(Icons.Default.Palette, contentDescription = null, tint = gold) },
                    title = "Tema",
                    subtitle = "Pendiente (modo claro/oscuro)",
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    onClick = { onThemeClick?.invoke() }
                )
            }

            // =========================
            // ABOUT / TUTORIAL
            // =========================
            SectionCard(
                title = "FutureMe",
                titleColor = titleGold,
                cardBg = cardBg,
                gold = gold
            ) {
                SettingRow(
                    icon = { Icon(Icons.Default.Info, contentDescription = null, tint = gold) },
                    title = "Sobre FutureMe",
                    subtitle = "Qué es la app y cómo funciona",
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    onClick = { showAboutDialog = true }
                )

                Divider(color = gold.copy(alpha = 0.10f), thickness = 1.dp)

                SettingRow(
                    icon = { Icon(Icons.Default.PlayCircle, contentDescription = null, tint = gold) },
                    title = "Ver tutorial otra vez",
                    subtitle = "Vuelve a mostrar los popups de bienvenida",
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    onClick = onShowTutorialAgain
                )
            }
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = Color(0xFF121212),
            title = {
                Text(
                    text = "Sobre FutureMe",
                    color = titleGold,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "FutureMe es una app de cápsulas del tiempo: escribes un mensaje hoy y lo abres en una fecha futura.",
                        color = textPrimary
                    )
                    Text(
                        text = "Puedes crear cápsulas privadas o compartidas y recibir avisos cuando se abran.",
                        color = textSecondary
                    )
                    Text(
                        text = "Este tutorial se puede volver a ver desde Ajustes.",
                        color = textSecondary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Cerrar", color = gold, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    titleColor: Color,
    cardBg: Color,
    gold: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, gold.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = {
                Text(
                    text = title,
                    color = titleColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                content()
            }
        )
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
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp),
                contentAlignment = Alignment.Center
            ) { icon() }

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
        }
    }
}
