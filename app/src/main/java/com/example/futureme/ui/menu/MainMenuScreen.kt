package com.example.futureme.ui.menu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource // ✅ Importante para las traducciones
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futureme.R
import com.example.futureme.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    isDark: Boolean,
    onMenuClick: (() -> Unit)?,
    onGoToCapsules: () -> Unit,
    onGoToProfile: () -> Unit,
    onGoToSettings: () -> Unit
) {
    // 🎨 CONSUMIMOS EL THEME DIRECTAMENTE
    val gold = MaterialTheme.colorScheme.primary
    val textPrimary = MaterialTheme.colorScheme.onSurface

    // Degradado Seda/Arena usando tus constantes de Color.kt
    val cardBrush = if (isDark) {
        Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, AzulProfundo))
    } else {
        Brush.verticalGradient(listOf(ClaroBase, ClaroSuave, ClaroPrincipal))
    }

    val cardBorder = MaterialTheme.colorScheme.outline.copy(alpha = if(isDark) 0.4f else 0.8f)

    AppBackground(type = BackgroundType.LOGIN_IMAGE, isDark = isDark) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            // ✅ CORREGIDO: Usar recurso
                            Text(
                                text = stringResource(R.string.menu_title),
                                color = gold,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                        },
                        navigationIcon = {
                            if (onMenuClick != null) {
                                IconButton(onClick = onMenuClick) {
                                    // ✅ CORREGIDO: Usar recurso
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = stringResource(R.string.content_desc_menu),
                                        tint = gold
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                    Divider(color = gold.copy(alpha = 0.2f), thickness = 1.dp)
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // -------- BOTÓN PRINCIPAL --------
                Surface(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.5.dp, cardBorder),
                    shadowElevation = if (isDark) 12.dp else 6.dp,
                    onClick = onGoToCapsules
                ) {
                    Box(modifier = Modifier.background(cardBrush), contentAlignment = Alignment.Center) {
                        // ✅ CORREGIDO: Usar recurso
                        Text(
                            text = stringResource(R.string.home_title),
                            color = textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                // -------- BOTONES SECUNDARIOS --------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    GoldImageCardButton(
                        label = stringResource(R.string.menu_profile), // ✅ CORREGIDO
                        drawableRes = R.drawable.ic_profile_gold,
                        onClick = onGoToProfile,
                        modifier = Modifier.weight(1f),
                        textPrimary = textPrimary,
                        cardBrush = cardBrush,
                        borderColor = cardBorder,
                        isDark = isDark
                    )

                    GoldImageCardButton(
                        label = stringResource(R.string.menu_settings), // ✅ CORREGIDO
                        drawableRes = R.drawable.ic_settings_gold,
                        onClick = onGoToSettings,
                        modifier = Modifier.weight(1f),
                        textPrimary = textPrimary,
                        cardBrush = cardBrush,
                        borderColor = cardBorder,
                        isDark = isDark
                    )
                }
            }
        }
    }
}

@Composable
private fun GoldImageCardButton(
    label: String,
    drawableRes: Int,
    onClick: () -> Unit,
    modifier: Modifier,
    textPrimary: Color,
    cardBrush: Brush,
    borderColor: Color,
    isDark: Boolean
) {
    Surface(
        modifier = modifier.height(170.dp),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.2.dp, borderColor),
        shadowElevation = if (isDark) 10.dp else 4.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize().background(cardBrush).padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = label,
                modifier = Modifier.size(95.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = label.uppercase(),
                color = textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }
    }
}