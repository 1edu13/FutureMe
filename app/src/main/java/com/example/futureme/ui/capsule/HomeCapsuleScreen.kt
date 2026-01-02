package com.example.futureme.ui.capsule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource // ✅ Import necesario
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futureme.R // ✅ Import necesario
import com.example.futureme.data.model.Capsule
import com.example.futureme.ui.theme.*
import java.text.DateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeCapsuleScreen(
    isDark: Boolean,
    userId: String?,
    onMenuClick: (() -> Unit)?,
    capsuleViewModel: CapsuleViewModel,
    onCapsuleClick: (Capsule) -> Unit,
    onNavigateToCreate: () -> Unit,
    onJoinCapsule: () -> Unit
) {
    val capsules by capsuleViewModel.capsules.collectAsState()
    val isLoading by capsuleViewModel.isLoading.collectAsState()
    val error by capsuleViewModel.error.collectAsState()

    // ⛔ LOGICA INTACTA: loadCapsules() se llama tal cual estaba
    LaunchedEffect(userId) {
        if (userId != null) capsuleViewModel.loadCapsules()
    }

    // Definimos el degradado de las tarjetas usando los nuevos colores del tema
    val cardBrush = if (isDark) {
        Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, AzulProfundo))
    } else {
        Brush.verticalGradient(listOf(ClaroBase, ClaroSuave, ClaroPrincipal))
    }

    AppBackground(type = BackgroundType.GRADIENT, isDark = isDark) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            // ✅ CORREGIDO: Usar recurso
                            Text(
                                text = stringResource(R.string.home_title),
                                color = OroAmbar,
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
                                        tint = OroAmbar
                                    )
                                }
                            } else {
                                IconButton(onClick = { /* no-op */ }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = OroAmbar)
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                    Divider(color = OroAmbar.copy(alpha = 0.2f), thickness = 1.dp)
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNavigateToCreate,
                    containerColor = if (isDark) OroAmbar else AzulProfundo,
                    contentColor = if (isDark) AzulProfundo else Color.White
                ) {
                    // ✅ CORREGIDO: Usar recurso
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.content_desc_create)
                    )
                }
            }
        ) { innerPadding ->
            if (userId == null) {
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OroAmbar)
                }
                return@Scaffold
            }

            LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                // Botón Unirse (Tu lógica original de botón scrolleable)
                item {
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onJoinCapsule,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OroAmbar)
                    ) {
                        // ✅ CORREGIDO: Usar recurso
                        Text(
                            text = stringResource(R.string.btn_join_capsule),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ... (Lógica de Error y Loading intacta) ...
                if (isLoading && capsules.isEmpty()) {
                    item { Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = OroAmbar) } }
                }

                if (capsules.isEmpty() && !isLoading) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.5f))
                        ) {
                            Column(modifier = Modifier.background(cardBrush).padding(20.dp)) {
                                // ✅ CORREGIDOS: Textos de estado vacío
                                Text(
                                    text = stringResource(R.string.empty_capsules_title),
                                    color = OroAmbar,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = stringResource(R.string.empty_capsules_msg),
                                    color = MaterialTheme.colorScheme.onBackground.copy(0.7f)
                                )
                            }
                        }
                    }
                } else {
                    items(capsules) { capsule ->
                        CapsuleItem(capsule, cardBrush, isDark) { onCapsuleClick(capsule) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CapsuleItem(
    capsule: Capsule,
    cardBrush: Brush,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val isOpenable = capsule.isOpenable()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
            .alpha(if (isOpenable) 1f else 0.85f),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline.copy(0.7f)),
        shadowElevation = if (isDark) 8.dp else 2.dp
    ) {
        Column(modifier = Modifier.background(cardBrush).padding(20.dp)) {
            Text(
                text = capsule.title,
                color = if (isOpenable) OroAmbar else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = capsule.text,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))

            // ✅ CORREGIDO: Lógica de etiqueta de estado
            val label = if (isOpenable) {
                stringResource(R.string.capsule_ready)
            } else {
                stringResource(R.string.capsule_wait)
            }

            Text(
                text = label,
                color = if (isOpenable) OroAmbar else MaterialTheme.colorScheme.onSurface.copy(0.6f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isOpenable) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}