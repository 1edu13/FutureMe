package com.example.futureme.ui.capsule

import android.content.ClipData
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.futureme.R
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

    // ✅ userId actual
    val currentUserId = remember { AuthRepository().getCurrentUser()?.uid }

    LaunchedEffect(capsuleId) {
        capsuleViewModel.loadCapsuleById(capsuleId)
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            Toast.makeText(context, "¡Contribución guardada!", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose { capsuleViewModel.clearSelectedCapsule() }
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
                            text = capsule?.title ?: "",
                            color = titleGold,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
            // Fondo
            Image(
                painter = painterResource(id = R.drawable.login_bg_dark),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.72f))
            )

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = gold)
                }

                error != null -> Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    color = Color(0xFF1A0E0E),
                    border = BorderStroke(1.dp, Color(0xFFB24A4A).copy(alpha = 0.55f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = error ?: "",
                        color = textPrimary,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                capsule != null -> {
                    CapsuleDetailContent(
                        capsule = capsule!!,
                        viewModel = capsuleViewModel,
                        context = context,
                        currentUserId = currentUserId,
                        gold = gold,
                        titleGold = titleGold,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        cardBg = cardBg
                    )
                }
            }
        }
    }
}

@Composable
private fun CapsuleDetailContent(
    capsule: Capsule,
    viewModel: CapsuleViewModel,
    context: Context,
    currentUserId: String?,
    gold: Color,
    titleGold: Color,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color
) {
    val isOwner = currentUserId != null && currentUserId == capsule.creatorId

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // ======================================================
        // 1) CÓDIGO INVITACIÓN (solo owner + compartida + editable + NO abierta)
        // ======================================================
        if (isOwner && capsule.isShared && capsule.isEditable() && !capsule.isOpenable()) {
            item {
                SectionCard(title = "Invitación", gold = gold, titleGold = titleGold, cardBg = cardBg) {
                    Text(
                        text = "Código de invitación",
                        color = textSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF121212),
                        border = BorderStroke(1.dp, gold.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = capsule.inviteCode,
                                color = textPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE)
                                        as android.content.ClipboardManager
                                val clip = ClipData.newPlainText("Código cápsula", capsule.inviteCode)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = gold)
                            }
                        }
                    }
                }
            }
        }

        // ======================================================
        // 2) CABECERA DE ESTADO (abierta/cerrada)
        // ======================================================
        item {
            val openDate = capsule.openDate.toDate()
            val stateTitle = if (capsule.isOpenable()) "Cápsula abierta" else "Cápsula cerrada"
            val stateIcon = if (capsule.isOpenable()) Icons.Default.LockOpen else Icons.Default.Lock

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = cardBg,
                shadowElevation = 10.dp,
                border = BorderStroke(1.dp, gold.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF121212),
                        border = BorderStroke(1.dp, gold.copy(alpha = 0.22f))
                    ) {
                        Box(Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                            Icon(stateIcon, contentDescription = null, tint = gold)
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stateTitle,
                            color = titleGold,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Se abre el: ${DateFormat.getDateTimeInstance().format(openDate)}",
                            color = textSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // ======================================================
        // 3) SI ESTÁ ABIERTA → SECCIONES POR USUARIO
        // ======================================================
        if (capsule.isOpenable()) {
            val entries = capsule.contributions.toList()

            if (entries.isEmpty()) {
                item {
                    SectionCard(title = "Contenido", gold = gold, titleGold = titleGold, cardBg = cardBg) {
                        Text(
                            text = "No hay contribuciones todavía.",
                            color = textSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                items(entries) { (userId, data) ->
                    val userText = data["text"] as? String ?: ""
                    val images = data["images"] as? List<String> ?: emptyList()

                    ContributionSection(
                        userId = userId,
                        isCreator = (userId == capsule.creatorId),
                        text = userText,
                        images = images,
                        gold = gold,
                        titleGold = titleGold,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        cardBg = cardBg
                    )
                }
            }
        }

        // ======================================================
        // 4) SI ESTÁ CERRADA → FORMULARIO SI EDITABLE
        // ======================================================
        else {
            item {
                if (capsule.isEditable()) {
                    val deadline = capsule.editDeadline.toDate()

                    SectionCard(title = "Tu contribución", gold = gold, titleGold = titleGold, cardBg = cardBg) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = gold)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Tienes hasta el ${DateFormat.getDateTimeInstance().format(deadline)}",
                                color = textSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        ContributionFormStyled(
                            capsuleId = capsule.id,
                            viewModel = viewModel,
                            context = context,
                            gold = gold,
                            titleGold = titleGold,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary
                        )
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF1A0E0E),
                        border = BorderStroke(1.dp, Color(0xFFB24A4A).copy(alpha = 0.55f))
                    ) {
                        Text(
                            text = "El periodo de aportaciones ha finalizado. Ahora solo queda esperar a que se abra.",
                            color = textPrimary,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContributionSection(
    userId: String,
    isCreator: Boolean,
    text: String,
    images: List<String>,
    gold: Color,
    titleGold: Color,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF121212),
                    border = BorderStroke(1.dp, gold.copy(alpha = 0.20f))
                ) {
                    Box(Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = gold)
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isCreator) "Creador" else "Participante",
                        color = titleGold,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = userId.take(10) + "…",
                        color = textSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Carrusel grande
            if (images.isNotEmpty()) {
                ImageCarousel(
                    urls = images,
                    gold = gold
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF121212),
                    border = BorderStroke(1.dp, gold.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = gold.copy(alpha = 0.65f))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Sin imágenes",
                            color = textSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Texto con marco bonito
            if (text.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF121212),
                    border = BorderStroke(1.dp, gold.copy(alpha = 0.18f))
                ) {
                    Text(
                        text = text,
                        color = textPrimary,
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF121212),
                    border = BorderStroke(1.dp, gold.copy(alpha = 0.12f))
                ) {
                    Text(
                        text = "Sin texto",
                        color = textSecondary,
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageCarousel(
    urls: List<String>,
    gold: Color
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(urls) { url ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF111111),
                border = BorderStroke(1.dp, gold.copy(alpha = 0.20f))
            ) {
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier
                        .width(260.dp)
                        .height(170.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun ContributionFormStyled(
    capsuleId: String,
    viewModel: CapsuleViewModel,
    context: Context,
    gold: Color,
    titleGold: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    var text by remember { mutableStateOf("") }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> selectedImageUris = uris }
    )

    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text("Escribe algo para el futuro...") },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp),
        minLines = 4,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = gold,
            focusedLabelColor = gold,
            cursorColor = gold,
            unfocusedBorderColor = gold.copy(alpha = 0.25f),
            unfocusedLabelColor = textSecondary,
            focusedTextColor = textPrimary,
            unfocusedTextColor = textPrimary
        )
    )

    OutlinedButton(
        onClick = {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, gold.copy(alpha = 0.35f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color(0xFF121212),
            contentColor = textPrimary
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = gold)
        Spacer(modifier = Modifier.width(10.dp))
        Text("Adjuntar imágenes", fontWeight = FontWeight.SemiBold)
    }

    if (selectedImageUris.isNotEmpty()) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(selectedImageUris) { uri ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF111111),
                    border = BorderStroke(1.dp, gold.copy(alpha = 0.18f))
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier.size(96.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    } else {
        Text(
            text = "Opcional: añade imágenes también.",
            color = textSecondary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.alpha(0.9f)
        )
    }

    Button(
        onClick = {
            viewModel.updateContribution(capsuleId, text, selectedImageUris, context)
            text = ""
            selectedImageUris = emptyList()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        enabled = text.isNotBlank() || selectedImageUris.isNotEmpty(),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF121212),
            contentColor = textPrimary,
            disabledContainerColor = Color(0xFF101010),
            disabledContentColor = textPrimary.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, gold.copy(alpha = 0.45f))
    ) {
        Text("Guardar mi parte", fontWeight = FontWeight.SemiBold)
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
