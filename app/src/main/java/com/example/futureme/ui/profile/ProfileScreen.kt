package com.example.futureme.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.futureme.R
import com.example.futureme.ui.auth.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onGoToEditAccount: () -> Unit
) {
    val user by authViewModel.user.collectAsState()
    val error by authViewModel.error.collectAsState()
    val success by authViewModel.success.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    val gold = Color(0xFFD4AF37)
    val titleGold = Color(0xFFC9A84D)
    val textPrimary = Color(0xFFEDEDED)
    val textSecondary = textPrimary.copy(alpha = 0.75f)
    val bg = Color(0xFF0B0B0B)
    val cardBg = Color(0xFF0E0E0E)
    val toolbarBg = Color(0xFF161616)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Snackbars (error/success)
    LaunchedEffect(error) {
        if (!error.isNullOrBlank()) {
            snackbarHostState.showSnackbar(error!!)
            authViewModel.clearMessages()
        }
    }
    LaunchedEffect(success) {
        if (!success.isNullOrBlank()) {
            snackbarHostState.showSnackbar(success!!)
            authViewModel.clearMessages()
        }
    }

    // Delete dialog state
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }

    Scaffold(
        containerColor = bg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.profile_title),
                            color = titleGold,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
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

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ---- Cuenta (Nombre/Email) ----
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, gold.copy(alpha = 0.25f)),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Text(
                            text = stringResource(R.string.section_account),
                            color = titleGold,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.height(10.dp))

                        val displayName = user?.displayName?.takeIf { it.isNotBlank() } ?: stringResource(R.string.lbl_no_name)
                        val email = user?.email ?: stringResource(R.string.lbl_no_email)

                        Text(
                            text = stringResource(R.string.placeholder_name),
                            color = textSecondary,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = displayName,
                            color = textPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = stringResource(R.string.placeholder_email),
                            color = textSecondary,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = email,
                            color = textPrimary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // ---- Estadísticas (placeholder) ----
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, gold.copy(alpha = 0.25f)),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.section_stats),
                            color = titleGold,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatChip(
                                modifier = Modifier.weight(1f),
                                title = stringResource(R.string.stat_created),
                                value = "—",
                                gold = gold,
                                cardBg = Color(0xFF121212),
                                textPrimary = textPrimary
                            )
                            StatChip(
                                modifier = Modifier.weight(1f),
                                title = stringResource(R.string.stat_opened),
                                value = "—",
                                gold = gold,
                                cardBg = Color(0xFF121212),
                                textPrimary = textPrimary
                            )
                            StatChip(
                                modifier = Modifier.weight(1f),
                                title = stringResource(R.string.stat_pending),
                                value = "—",
                                gold = gold,
                                cardBg = Color(0xFF121212),
                                textPrimary = textPrimary
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.desc_stats_placeholder),
                            color = textSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // ---- Acciones ----
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, gold.copy(alpha = 0.25f)),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.section_actions),
                            color = titleGold,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Button(
                            onClick = onGoToEditAccount,
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF121212),
                                contentColor = textPrimary
                            ),
                            border = BorderStroke(1.dp, gold.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = gold)
                            Spacer(Modifier.width(10.dp))
                            Text(stringResource(R.string.btn_edit_account))
                        }

                        Button(
                            onClick = { showDeleteDialog = true },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1A0E0E),
                                contentColor = textPrimary
                            ),
                            border = BorderStroke(1.dp, Color(0xFFB24A4A).copy(alpha = 0.55f)),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFB24A4A))
                            Spacer(Modifier.width(10.dp))
                            Text(stringResource(R.string.btn_delete_account))
                        }
                    }
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = gold)
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!isLoading) showDeleteDialog = false },
            title = { Text(stringResource(R.string.dialog_delete_title), color = titleGold, fontWeight = FontWeight.SemiBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        stringResource(R.string.dialog_delete_body),
                        color = textPrimary
                    )
                    OutlinedTextField(
                        value = deletePassword,
                        onValueChange = { deletePassword = it },
                        singleLine = true,
                        label = { Text(stringResource(R.string.lbl_current_password)) },
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
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            authViewModel.deleteAccount(deletePassword)
                            deletePassword = ""
                            showDeleteDialog = false
                        }
                    },
                    enabled = !isLoading
                ) {
                    Text(stringResource(R.string.btn_delete_confirm), color = Color(0xFFB24A4A), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        deletePassword = ""
                        showDeleteDialog = false
                    },
                    enabled = !isLoading
                ) {
                    Text(stringResource(R.string.cancel), color = textPrimary)
                }
            },
            containerColor = Color(0xFF121212)
        )
    }
}

@Composable
private fun StatChip(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    gold: Color,
    cardBg: Color,
    textPrimary: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = BorderStroke(1.dp, gold.copy(alpha = 0.20f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = textPrimary.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = value,
                color = textPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}