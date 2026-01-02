package com.example.futureme.ui.capsule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.futureme.R
import com.example.futureme.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinCapsuleScreen(
    capsuleViewModel: CapsuleViewModel,
    onNavigateBack: () -> Unit,
    onJoined: () -> Unit,
    isDark: Boolean = isSystemInDarkTheme()
) {
    val isLoading by capsuleViewModel.isLoading.collectAsState()
    val error by capsuleViewModel.error.collectAsState()
    val saveSuccess by capsuleViewModel.saveSuccess.collectAsState()

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) onJoined()
    }

    JoinCapsuleScreenContent(
        isLoading = isLoading,
        error = error,
        onJoin = { code -> capsuleViewModel.joinCapsule(code) },
        onNavigateBack = onNavigateBack,
        isDark = isDark
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinCapsuleScreenContent(
    isLoading: Boolean,
    error: String?,
    onJoin: (String) -> Unit,
    onNavigateBack: () -> Unit,
    isDark: Boolean
) {
    // 🎨 Paleta igual que Settings (según tu Color.kt)
    val gold = OroAmbar
    val titleColor = if (isDark) gold else AzulProfundo
    val textPrimary = if (isDark) BlancoAzulado else AzulProfundo
    val textSecondary = if (isDark) GrisAzulado else ClaroBorde

    // Card premium (degradado dentro del “marco”, encima del fondo radial)
    val cardBrush = if (isDark) {
        Brush.verticalGradient(listOf(AzulSuperficie, AzulProfundo))
    } else {
        Brush.verticalGradient(listOf(ClaroBase, ClaroSuave, ClaroPrincipal))
    }
    val cardBorder = if (isDark) gold.copy(alpha = 0.30f) else ClaroBorde.copy(alpha = 0.80f)

    var code by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val canJoin = !isLoading && code.trim().isNotBlank()

    // ✅ SOLO degradado (sin imagen) usando tu AppBackground
    AppBackground(type = BackgroundType.GRADIENT, isDark = isDark) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.join_capsule_title),
                            color = gold,
                            fontWeight = FontWeight.Black
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack, enabled = !isLoading) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = gold
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = gold,
                        navigationIconContentColor = gold
                    )
                )
            }
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.5.dp, cardBorder),
                    shadowElevation = if (isDark) 10.dp else 6.dp,
                    color = Color.Transparent
                ) {
                    Column(
                        modifier = Modifier
                            .background(cardBrush)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.lbl_enter_code),
                            color = gold,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = stringResource(R.string.desc_join_code),
                            color = textSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )

                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it },
                            label = { Text(stringResource(R.string.hint_capsule_code)) },
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

                                unfocusedBorderColor = cardBorder.copy(alpha = 0.70f),
                                unfocusedLabelColor = textSecondary,

                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary,

                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        if (!error.isNullOrBlank()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.45f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        if (isLoading) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(color = gold)
                            }
                        } else {
                            Button(
                                onClick = { onJoin(code.trim()) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                enabled = canJoin,
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = textPrimary,
                                    disabledContainerColor = Color.Transparent,
                                    disabledContentColor = textPrimary.copy(alpha = 0.35f)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    gold.copy(alpha = if (canJoin) 0.55f else 0.22f)
                                )
                            ) {
                                Text(stringResource(R.string.btn_join_action), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
            }
        }
    }
}