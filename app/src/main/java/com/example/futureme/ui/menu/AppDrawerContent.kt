package com.example.futureme.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.futureme.ui.navigation.Screen
import com.example.futureme.ui.theme.*

@Composable
fun AppDrawerContent(
    userEmail: String,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    isDark: Boolean
) {
    val gold = OroAmbar
    val textPrimary = if (isDark) BlancoAzulado else AzulProfundo
    val textSecondary = if (isDark) GrisAzulado else ClaroBorde

    val sheetBrush = if (isDark) {
        Brush.verticalGradient(listOf(AzulProfundo, AzulSuperficie))
    } else {
        Brush.verticalGradient(listOf(ClaroPrincipal, ClaroSuave, ClaroBase))
    }

    val selectedBg = if (isDark) gold.copy(alpha = 0.16f) else AzulProfundo.copy(alpha = 0.10f)
    val dividerColor = gold.copy(alpha = 0.18f)

    ModalDrawerSheet(
        drawerContainerColor = Color.Transparent,
        drawerContentColor = textPrimary
    ) {
        Surface(
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxHeight()
                .background(sheetBrush)
                .padding(vertical = 12.dp)
        ) {
            Column {
                Text(
                    text = "FutureMe",
                    style = MaterialTheme.typography.titleLarge,
                    color = textPrimary,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodySmall,
                    color = textSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(12.dp))
                Divider(color = dividerColor)

                Spacer(Modifier.height(8.dp))

                DrawerRow(
                    label = "Menú",
                    icon = Icons.Default.Menu,
                    selected = currentRoute == Screen.MainMenu.route,
                    selectedBg = selectedBg,
                    iconTint = gold,
                    textColor = textPrimary,
                    onClick = { onNavigate(Screen.MainMenu.route) }
                )

                DrawerRow(
                    label = "Mis cápsulas",
                    icon = Icons.Default.Home,
                    selected = currentRoute == Screen.Home.route,
                    selectedBg = selectedBg,
                    iconTint = gold,
                    textColor = textPrimary,
                    onClick = { onNavigate(Screen.Home.route) }
                )

                DrawerRow(
                    label = "Crear cápsula",
                    icon = Icons.Default.AddCircle,
                    selected = currentRoute == Screen.CreateCapsule.route,
                    selectedBg = selectedBg,
                    iconTint = gold,
                    textColor = textPrimary,
                    onClick = { onNavigate(Screen.CreateCapsule.route) }
                )

                DrawerRow(
                    label = "Unirse a cápsula",
                    icon = Icons.Default.Link,
                    selected = currentRoute == Screen.JoinCapsule.route,
                    selectedBg = selectedBg,
                    iconTint = gold,
                    textColor = textPrimary,
                    onClick = { onNavigate(Screen.JoinCapsule.route) }
                )

                Spacer(Modifier.height(10.dp))
                Divider(color = dividerColor)
                Spacer(Modifier.height(10.dp))

                DrawerRow(
                    label = "Perfil",
                    icon = Icons.Default.Person,
                    selected = currentRoute == Screen.Profile.route,
                    selectedBg = selectedBg,
                    iconTint = gold,
                    textColor = textPrimary,
                    onClick = { onNavigate(Screen.Profile.route) }
                )

                DrawerRow(
                    label = "Ajustes",
                    icon = Icons.Default.Settings,
                    selected = currentRoute == Screen.Settings.route,
                    selectedBg = selectedBg,
                    iconTint = gold,
                    textColor = textPrimary,
                    onClick = { onNavigate(Screen.Settings.route) }
                )

                Spacer(Modifier.height(10.dp))
                Divider(color = dividerColor)
                Spacer(Modifier.height(10.dp))

                DrawerRow(
                    label = "Cerrar sesión",
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    selected = false,
                    selectedBg = selectedBg,
                    iconTint = gold,
                    textColor = textPrimary,
                    onClick = onLogout
                )
            }
        }
    }
}

@Composable
private fun DrawerRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    selectedBg: Color,
    iconTint: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    val bg = if (selected) selectedBg else Color.Transparent

    Row(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconTint)
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
