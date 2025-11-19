package com.example.futureme.ui.capsule

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.DateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapsuleDetailScreen(
    capsuleViewModel: CapsuleViewModel,
    capsuleId: String,
    onNavigateBack: () -> Unit
) {
    val capsule by capsuleViewModel.selectedCapsule.collectAsState()
    val isLoading by capsuleViewModel.isLoading.collectAsState()
    val error by capsuleViewModel.error.collectAsState()

    LaunchedEffect(capsuleId) {
        capsuleViewModel.loadCapsuleById(capsuleId)
    }

    // Clear selected capsule when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            capsuleViewModel.clearSelectedCapsule()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(capsule?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val currentError = error
            when {
                isLoading -> CircularProgressIndicator()
                currentError != null -> Text(text = currentError, color = MaterialTheme.colorScheme.error)
                capsule != null -> {
                    if (capsule!!.isOpenable()) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                            Text(text = capsule!!.text, style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(16.dp))
                            val creationDate = capsule!!.createdAt.toDate()
                            Text(
                                text = "Creada el: ${DateFormat.getDateTimeInstance().format(creationDate)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(text = "Esta cápsula aún no se puede abrir.")
                    }
                }
            }
        }
    }
}
