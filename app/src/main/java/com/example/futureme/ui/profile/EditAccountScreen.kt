package com.example.futureme.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.futureme.ui.auth.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAccountScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
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

    // Form state: change name
    var currentPasswordForName by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("") }

    // Form state: change password
    var currentPasswordForPass by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var repeatNewPassword by remember { mutableStateOf("") }

    // Snackbars
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

    Scaffold(
        containerColor = bg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Editar cuenta",
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

                // ===== Cambiar nombre =====
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = gold)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Cambiar nombre",
                                color = titleGold,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            singleLine = true,
                            label = { Text("Nuevo nombre") },
                            colors = fieldColors(gold)
                        )

                        OutlinedTextField(
                            value = currentPasswordForName,
                            onValueChange = { currentPasswordForName = it },
                            singleLine = true,
                            label = { Text("Contraseña actual") },
                            colors = fieldColors(gold)
                        )

                        Button(
                            onClick = {
                                authViewModel.updateDisplayName(
                                    newName = newName,
                                    currentPassword = currentPasswordForName
                                )
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF121212),
                                contentColor = textPrimary
                            ),
                            border = BorderStroke(1.dp, gold.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Guardar nombre", fontWeight = FontWeight.SemiBold)
                        }

                        Text(
                            text = "Para cambiar datos de cuenta pedimos la contraseña actual.",
                            color = textSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // ===== Cambiar contraseña =====
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = gold)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Cambiar contraseña",
                                color = titleGold,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedTextField(
                            value = currentPasswordForPass,
                            onValueChange = { currentPasswordForPass = it },
                            singleLine = true,
                            label = { Text("Contraseña actual") },
                            colors = fieldColors(gold)
                        )

                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            singleLine = true,
                            label = { Text("Nueva contraseña (mín. 6)") },
                            colors = fieldColors(gold)
                        )

                        OutlinedTextField(
                            value = repeatNewPassword,
                            onValueChange = { repeatNewPassword = it },
                            singleLine = true,
                            label = { Text("Repetir nueva contraseña") },
                            colors = fieldColors(gold)
                        )

                        Button(
                            onClick = {
                                if (newPassword != repeatNewPassword) {
                                    scope.launch { snackbarHostState.showSnackbar("Las contraseñas no coinciden") }
                                } else {
                                    authViewModel.updatePassword(
                                        currentPassword = currentPasswordForPass,
                                        newPassword = newPassword
                                    )
                                }
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF121212),
                                contentColor = textPrimary
                            ),
                            border = BorderStroke(1.dp, gold.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Guardar contraseña", fontWeight = FontWeight.SemiBold)
                        }

                        Text(
                            text = "Firebase requiere reautenticación antes de cambios sensibles.",
                            color = textSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun fieldColors(gold: Color) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = gold,
    focusedLabelColor = gold,
    cursorColor = gold,
    unfocusedBorderColor = gold.copy(alpha = 0.25f),
    unfocusedLabelColor = Color(0xFFEDEDED).copy(alpha = 0.75f),
    focusedTextColor = Color(0xFFEDEDED),
    unfocusedTextColor = Color(0xFFEDEDED)
)
