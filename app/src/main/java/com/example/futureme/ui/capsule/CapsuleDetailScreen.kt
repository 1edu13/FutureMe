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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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

    // Cargar cápsula
    LaunchedEffect(capsuleId) {
        capsuleViewModel.loadCapsuleById(capsuleId)
    }

    // Borrar cápsula seleccionada al salir
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
            contentAlignment = Alignment.TopCenter
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
                    val context = LocalContext.current

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {

                        // ======================================================
                        // 1) SIEMPRE mostrar el código de invitación
                        // ======================================================
                        Text(
                            text = "Código de invitación",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = cap.id,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )

                            Button(onClick = {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                                        as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Código cápsula", cap.id)
                                clipboard.setPrimaryClip(clip)
                            }) {
                                Text("Copiar")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))

                        // ======================================================
                        // 2) SI NO SE PUEDE ABRIR → mensaje y salir
                        // ======================================================
                        if (!cap.isOpenable()) {
                            Text(
                                text = "Esta cápsula aún no se puede abrir.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            return@Column
                        }

                        // ======================================================
                        // 3) CONTENIDO (solo si se puede abrir)
                        // ======================================================
                        Text(
                            text = cap.text,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(16.dp))

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
                                            .padding(end = 8.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
