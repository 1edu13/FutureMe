package com.example.futureme.ui.profile

import android.content.Context
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.futureme.R
import com.example.futureme.ui.auth.AuthViewModel
import com.example.futureme.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAccountScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    isDark: Boolean
) {
    val error by authViewModel.error.collectAsState()
    val success by authViewModel.success.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    val gold = OroAmbar
    val titleGold = if (isDark) OroAmbar else AzulProfundo
    val textPrimary = if (isDark) BlancoAzulado else AzulProfundo
    val textSecondary = if (isDark) GrisAzulado else ClaroBorde

    val cardBrush = if (isDark) {
        Brush.verticalGradient(listOf(AzulSuperficie, AzulProfundo))
    } else {
        Brush.verticalGradient(listOf(ClaroBase, ClaroSuave, ClaroPrincipal))
    }
    val cardBorder = if (isDark) gold.copy(alpha = 0.30f) else ClaroBorde.copy(alpha = 0.80f)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var currentPasswordForName by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("") }

    var currentPasswordForPass by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var repeatNewPassword by remember { mutableStateOf("") }

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

    AppBackground(type = BackgroundType.GRADIENT, isDark = isDark) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.edit_account_title),
                                color = gold,
                                fontWeight = FontWeight.Black
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
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                    Divider(color = gold.copy(alpha = 0.18f), thickness = 1.dp)
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
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, cardBorder),
                        shadowElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(cardBrush)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = gold)
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    stringResource(R.string.section_change_name),
                                    color = titleGold,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            OutlinedTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                singleLine = true,
                                label = { Text(stringResource(R.string.lbl_new_name)) },
                                enabled = !isLoading,
                                colors = themedFieldColors(gold, textPrimary, textSecondary)
                            )

                            OutlinedTextField(
                                value = currentPasswordForName,
                                onValueChange = { currentPasswordForName = it },
                                singleLine = true,
                                label = { Text(stringResource(R.string.lbl_current_password)) },
                                enabled = !isLoading,
                                colors = themedFieldColors(gold, textPrimary, textSecondary)
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
                                    containerColor = Color.Transparent,
                                    contentColor = textPrimary,
                                    disabledContainerColor = Color.Transparent,
                                    disabledContentColor = textPrimary.copy(alpha = 0.35f)
                                ),
                                border = BorderStroke(1.dp, gold.copy(alpha = 0.35f)),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.btn_save_name), fontWeight = FontWeight.SemiBold)
                            }

                            Text(
                                text = stringResource(R.string.desc_change_name),
                                color = textSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }


                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, cardBorder),
                        shadowElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(cardBrush)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = gold)
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    stringResource(R.string.section_change_password),
                                    color = titleGold,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            OutlinedTextField(
                                value = currentPasswordForPass,
                                onValueChange = { currentPasswordForPass = it },
                                singleLine = true,
                                label = { Text(stringResource(R.string.lbl_current_password)) },
                                enabled = !isLoading,
                                colors = themedFieldColors(gold, textPrimary, textSecondary)
                            )

                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                singleLine = true,
                                label = { Text(stringResource(R.string.lbl_new_password)) },
                                enabled = !isLoading,
                                colors = themedFieldColors(gold, textPrimary, textSecondary)
                            )

                            OutlinedTextField(
                                value = repeatNewPassword,
                                onValueChange = { repeatNewPassword = it },
                                singleLine = true,
                                label = { Text(stringResource(R.string.lbl_repeat_password)) },
                                enabled = !isLoading,
                                colors = themedFieldColors(gold, textPrimary, textSecondary)
                            )

                            val mismatchMsg = stringResource(R.string.msg_passwords_mismatch)
                            Button(
                                onClick = {
                                    if (newPassword != repeatNewPassword) {
                                        scope.launch { snackbarHostState.showSnackbar(mismatchMsg) }
                                    } else {
                                        authViewModel.updatePassword(
                                            currentPassword = currentPasswordForPass,
                                            newPassword = newPassword
                                        )
                                    }
                                },
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = textPrimary,
                                    disabledContainerColor = Color.Transparent,
                                    disabledContentColor = textPrimary.copy(alpha = 0.35f)
                                ),
                                border = BorderStroke(1.dp, gold.copy(alpha = 0.35f)),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.btn_save_password), fontWeight = FontWeight.SemiBold)
                            }

                            Text(
                                text = stringResource(R.string.desc_change_password),
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun themedFieldColors(
    gold: Color,
    textPrimary: Color,
    textSecondary: Color
) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = gold,
    focusedLabelColor = gold,
    cursorColor = gold,
    unfocusedBorderColor = gold.copy(alpha = 0.25f),
    unfocusedLabelColor = textSecondary,
    focusedTextColor = textPrimary,
    unfocusedTextColor = textPrimary,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent
)
