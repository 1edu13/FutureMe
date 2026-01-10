package com.example.futureme.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futureme.R
import com.example.futureme.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDark: Boolean,
    currentLanguageCode: String,
    onToggleTheme: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onShowTutorialAgain: () -> Unit
) {
    val gold = OroAmbar
    val textPrimary = if (isDark) BlancoAzulado else AzulProfundo
    val textSecondary = if (isDark) GrisAzulado else ClaroBorde

    val cardBrush = if (isDark) {
        Brush.verticalGradient(listOf(AzulSuperficie, AzulProfundo))
    } else {
        Brush.verticalGradient(listOf(ClaroBase, ClaroSuave, ClaroPrincipal))
    }

    val cardBorder = if (isDark) gold.copy(alpha = 0.3f) else ClaroBorde.copy(alpha = 0.8f)

    var showLanguageDialog by remember { mutableStateOf(false) }

    val languages = listOf(
        "es" to "Español",
        "en" to "English",
        "cs" to "Čeština"
    )

    val currentLanguageLabel = languages.find { it.first == currentLanguageCode }?.second
        ?: stringResource(R.string.setting_language_desc)

    AppBackground(type = BackgroundType.GRADIENT, isDark = isDark) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.settings_title),
                            color = gold,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = gold)
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

                // Cuadro principal de Ajustes
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.5.dp, cardBorder),
                    shadowElevation = if (isDark) 8.dp else 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .background(cardBrush)
                            .padding(20.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.section_appearance),
                            color = gold,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(Modifier.height(16.dp))

                        ThemeRow(
                            icon = { Icon(Icons.Default.Palette, contentDescription = null, tint = gold) },
                            title = stringResource(R.string.setting_dark_mode),
                            subtitle = stringResource(R.string.setting_dark_mode_desc),
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
                            title = stringResource(R.string.setting_language),
                            subtitle = currentLanguageLabel,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onClick = { showLanguageDialog = true }
                        )

                        Spacer(Modifier.height(24.dp))

                        Text(
                            text = stringResource(R.string.section_help),
                            color = gold,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(Modifier.height(16.dp))

                        SettingRow(
                            icon = { Icon(Icons.Default.PlayCircle, contentDescription = null, tint = gold) },
                            title = stringResource(R.string.setting_what_is),
                            subtitle = stringResource(R.string.setting_what_is_desc),
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
                            title = stringResource(R.string.setting_version),
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

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            containerColor = if (isDark) AzulSuperficie else ClaroSuave,
            title = {
                Text(
                    text = stringResource(R.string.setting_language),
                    color = gold,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    languages.forEach { (code, label) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (code == currentLanguageCode),
                                    onClick = {
                                        onLanguageChange(code)
                                        showLanguageDialog = false
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (code == currentLanguageCode),
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = gold,
                                    unselectedColor = textSecondary
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = label,
                                color = textPrimary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.cancel), color = gold)
                }
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