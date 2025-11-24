package com.example.futureme.ui.capsule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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

    DisposableEffect(Unit) {
        onDispose { capsuleViewModel.clearSelectedCapsule() }
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
            when {
                isLoading -> CircularProgressIndicator()

                error != null -> Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )

                capsule != null -> {
                    val cap = capsule!!

                    if (cap.isOpenable()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            // TEXTO
                            Text(text = cap.text, style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(16.dp))

                            // FECHA
                            val creationDate = cap.createdAt.toDate()
                            Text(
                                text = "Creada el: ${
                                    DateFormat.getDateTimeInstance().format(creationDate)
                                }",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(24.dp))


                            val images = cap.images ?: emptyList()
                            if (images.isNotEmpty()) {
                                Text(
                                    text = "Imágenes adjuntas:",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                LazyRow {
                                    items(images) { url ->
                                        AsyncImage(
                                            model = url,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(120.dp)
                                                .padding(end = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Text(text = "Esta cápsula aún no se puede abrir.")
                    }
                }
            }
        }
    }
}
