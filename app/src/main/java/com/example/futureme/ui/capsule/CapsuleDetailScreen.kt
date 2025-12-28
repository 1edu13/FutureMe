package com.example.futureme.ui.capsule

import android.content.ClipData
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.futureme.data.model.Capsule
import com.example.futureme.data.repository.AuthRepository
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
    val saveSuccess by capsuleViewModel.saveSuccess.collectAsState()

    val context = LocalContext.current

    // ✅ userId actual (si cambia, recompondrá)
    val currentUserId = remember {
        AuthRepository().getCurrentUser()?.uid
    }

    // Cargar cápsula
    LaunchedEffect(capsuleId) {
        capsuleViewModel.loadCapsuleById(capsuleId)
    }

    // Si se guarda la contribución, avisar
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            Toast.makeText(context, "¡Contribución guardada!", Toast.LENGTH_SHORT).show()
        }
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
                isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                error != null -> Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )

                capsule != null -> {
                    CapsuleDetailContent(
                        capsule = capsule!!,
                        viewModel = capsuleViewModel,
                        context = context,
                        currentUserId = currentUserId
                    )
                }
            }
        }
    }
}

@Composable
fun CapsuleDetailContent(
    capsule: Capsule,
    viewModel: CapsuleViewModel,
    context: Context,
    currentUserId: String?
) {
    val isOwner = currentUserId != null && currentUserId == capsule.creatorId

    // Usamos LazyColumn para que todo sea scrolleable
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {

        // ======================================================
        // 1) CÓDIGO DE INVITACIÓN (SOLO OWNER)
        // ======================================================
        if (isOwner && capsule.isShared && capsule.isEditable() && !capsule.isOpenable()) {
            item {
                Text(
                    text = "Código de invitación",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = capsule.inviteCode,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Button(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE)
                                as android.content.ClipboardManager
                        val clip = ClipData.newPlainText(
                            "Código cápsula",
                            capsule.inviteCode   // ✅ AHORA ES EL REAL
                        )
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Copiar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // ======================================================
        // 2) SI ESTÁ ABIERTA → MOSTRAR CONTENIDO DE TODOS
        // ======================================================
        if (capsule.isOpenable()) {
            item {
                Text(
                    text = "🎉 ¡La cápsula se ha abierto! 🎉",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Iterar sobre las contribuciones de cada usuario
            items(capsule.contributions.toList()) { (userId, data) ->
                val text = data["text"] as? String ?: ""
                val images = data["images"] as? List<String> ?: emptyList()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (userId == capsule.creatorId) "Creador" else "Participante",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (text.isNotEmpty()) {
                            Text(text = text, style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        if (images.isNotEmpty()) {
                            LazyRow {
                                items(images) { url ->
                                    AsyncImage(
                                        model = url,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(100.dp)
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

        // ======================================================
        // 3) SI ESTÁ CERRADA → MOSTRAR CUENTA ATRÁS Y FORMULARIO
        // ======================================================
        else {
            item {
                Text(
                    text = "🔒 Esta cápsula está cerrada.",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))

                val openDate = capsule.openDate.toDate()
                Text(
                    text = "Se abrirá el: ${DateFormat.getDateTimeInstance().format(openDate)}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(24.dp))
                Divider()
                Spacer(modifier = Modifier.height(24.dp))

                // Mostrar formulario SOLO si aún está en periodo de edición
                if (capsule.isEditable()) {
                    val deadline = capsule.editDeadline.toDate()
                    Text(
                        text = "⏳ Tienes hasta el ${DateFormat.getDateTimeInstance().format(deadline)} para añadir algo a la capsula.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    ContributionForm(capsuleId = capsule.id, viewModel = viewModel, context = context)
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⛔ El periodo de aportaciones ha finalizado. Ahora solo queda esperar a que se abra.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContributionForm(
    capsuleId: String,
    viewModel: CapsuleViewModel,
    context: Context
) {
    var text by remember { mutableStateOf("") }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> selectedImageUris = uris }
    )

    Column {
        Text(
            text = "Añade algo a la cápsula",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "No se podrá ver hasta que la cápsula se abra.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Escribe algo para el futuro...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Adjuntar Imágenes")
        }

        if (selectedImageUris.isNotEmpty()) {
            LazyRow(modifier = Modifier.padding(top = 8.dp)) {
                items(selectedImageUris) { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .padding(end = 4.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.updateContribution(capsuleId, text, selectedImageUris, context)
                text = ""
                selectedImageUris = emptyList()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = text.isNotBlank() || selectedImageUris.isNotEmpty()
        ) {
            Text("Guardar mi parte")
        }
    }
}
