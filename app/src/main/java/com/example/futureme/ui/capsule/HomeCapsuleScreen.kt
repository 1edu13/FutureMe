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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.futureme.data.model.Capsule
import java.text.DateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeCapsuleScreen(
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

    LaunchedEffect(userId) {
        if (userId != null) capsuleViewModel.loadCapsules()
    }

    // ---- Paleta ----
    val gold = Color(0xFFD4AF37)
    val titleGold = Color(0xFFC9A84D)
    val textPrimary = Color(0xFFEDEDED)
    val textSecondary = textPrimary.copy(alpha = 0.75f)
    val bg = Color(0xFF0B0B0B)
    val cardBg = Color(0xFF0E0E0E)
    val toolbarBg = Color(0xFF161616)

    Scaffold(
        containerColor = bg,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "Mis Cápsulas",
                            color = titleGold,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        if (onMenuClick != null) {
                            IconButton(onClick = onMenuClick) {
                                Icon(Icons.Default.Menu, contentDescription = "Menú", tint = gold)
                            }
                        } else {
                            IconButton(onClick = { /* no-op */ }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    tint = gold
                                )
                            }
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = Color(0xFF121212),
                contentColor = gold
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear nueva cápsula")
            }
        }
    ) { innerPadding ->

        if (userId == null) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = gold)
            }
            return@Scaffold
        }

        // ✅ TODO en un LazyColumn para que sea 100% scrolleable
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(bg),
            contentPadding = PaddingValues(bottom = 88.dp) // espacio para el FAB
        ) {

            // Botón unirse (scrollea con el resto)
            item {
                OutlinedButton(
                    onClick = onJoinCapsule,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    border = BorderStroke(1.dp, gold.copy(alpha = 0.35f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFF121212),
                        contentColor = textPrimary
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = "Unirse a cápsula",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Estado loading inicial
            if (isLoading && capsules.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = gold)
                    }
                }
            }

            // Error
            error?.let { msg ->
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color(0xFF1A0E0E),
                        border = BorderStroke(1.dp, Color(0xFFB24A4A).copy(alpha = 0.55f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = msg,
                            color = textPrimary,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Vacío
            if (capsules.isEmpty() && !isLoading) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(22.dp),
                        color = cardBg,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, gold.copy(alpha = 0.25f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "Aún no tienes cápsulas",
                                color = titleGold,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Pulsa + para crear la primera.",
                                color = textSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            } else {
                items(capsules) { capsule ->
                    CapsuleItem(
                        capsule = capsule,
                        gold = gold,
                        titleGold = titleGold,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        cardBg = cardBg,
                        onClick = { onCapsuleClick(capsule) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CapsuleItem(
    capsule: Capsule,
    gold: Color,
    titleGold: Color,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color,
    onClick: () -> Unit
) {
    val isOpenable = capsule.isOpenable()
    val openDate = capsule.openDate.toDate()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
            .alpha(if (isOpenable) 1f else 0.72f),
        shape = RoundedCornerShape(22.dp),
        color = cardBg,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, gold.copy(alpha = 0.20f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = capsule.title,
                color = titleGold,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = capsule.text,
                color = textPrimary,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            val label = if (isOpenable) {
                "¡Ya se puede abrir!"
            } else {
                "Se abre el: ${DateFormat.getDateInstance(DateFormat.MEDIUM).format(openDate)}"
            }

            Text(
                text = label,
                color = if (isOpenable) gold else textSecondary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isOpenable) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    gold: Color,
    titleGold: Color,
    cardBg: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        color = cardBg,
        shadowElevation = 10.dp,
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
                    color = titleGold,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                content()
            }
        )
    }
}
