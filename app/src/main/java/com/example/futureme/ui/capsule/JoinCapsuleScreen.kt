package com.example.futureme.ui.capsule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.futureme.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinCapsuleScreen(
    capsuleViewModel: CapsuleViewModel,
    onNavigateBack: () -> Unit,
    onJoined: () -> Unit
) {
    val isLoading by capsuleViewModel.isLoading.collectAsState()
    val error by capsuleViewModel.error.collectAsState()
    val saveSuccess by capsuleViewModel.saveSuccess.collectAsState()

    // Si se une correctamente → navegar
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) onJoined()
    }

    JoinCapsuleScreenContent(
        isLoading = isLoading,
        error = error,
        onJoin = { code -> capsuleViewModel.joinCapsule(code) },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinCapsuleScreenContent(
    isLoading: Boolean,
    error: String?,
    onJoin: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    // ---- Paleta ----
    val gold = Color(0xFFD4AF37)
    val titleGold = Color(0xFFC9A84D)
    val textPrimary = Color(0xFFEDEDED)
    val textSecondary = textPrimary.copy(alpha = 0.75f)
    val bg = Color(0xFF0B0B0B)
    val cardBg = Color(0xFF0E0E0E)
    val toolbarBg = Color(0xFF161616)

    var code by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val canJoin = !isLoading && code.trim().isNotBlank()

    Scaffold(
        containerColor = bg,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "Unirse a cápsula",
                            color = titleGold,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack, enabled = !isLoading) {
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
                        navigationIconContentColor = gold
                    )
                )
                Divider(color = gold.copy(alpha = 0.20f), thickness = 1.dp)
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Fondo (mismo que login si lo tienes)
            Image(
                painter = painterResource(id = R.drawable.login_bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            // Overlay oscuro
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.72f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState) // ✅ scrolleable
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = cardBg,
                    shadowElevation = 10.dp,
                    border = BorderStroke(1.dp, gold.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Introduce el código",
                            color = titleGold,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "Te unirás a una cápsula compartida usando su código.",
                            color = textSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )

                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it },
                            label = { Text("Código de la cápsula") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Link, contentDescription = null, tint = gold)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = gold,
                                focusedLabelColor = gold,
                                cursorColor = gold,
                                unfocusedBorderColor = gold.copy(alpha = 0.25f),
                                unfocusedLabelColor = textPrimary.copy(alpha = 0.75f),
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary
                            )
                        )
                    }
                }

                // Error bonito
                if (!error.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF1A0E0E),
                        border = BorderStroke(1.dp, Color(0xFFB24A4A).copy(alpha = 0.55f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = error,
                            color = textPrimary,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (isLoading) {
                    CircularProgressIndicator(color = gold)
                } else {
                    Button(
                        onClick = { onJoin(code.trim()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        enabled = canJoin,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF121212),
                            contentColor = textPrimary,
                            disabledContainerColor = Color(0xFF101010),
                            disabledContentColor = textPrimary.copy(alpha = 0.35f)
                        ),
                        border = BorderStroke(1.dp, gold.copy(alpha = if (canJoin) 0.45f else 0.18f))
                    ) {
                        Text("Unirme a la cápsula", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
