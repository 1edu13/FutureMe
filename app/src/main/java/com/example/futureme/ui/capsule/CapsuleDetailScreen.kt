package com.example.futureme.ui.capsule

import android.app.DownloadManager
import android.content.ClipData
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.futureme.R
import com.example.futureme.data.model.Capsule
import com.example.futureme.data.repository.AuthRepository
import com.example.futureme.ui.theme.*
import java.text.DateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapsuleDetailScreen(
    capsuleViewModel: CapsuleViewModel,
    capsuleId: String,
    onNavigateBack: () -> Unit,
    isDark: Boolean
) {
    val capsule by capsuleViewModel.selectedCapsule.collectAsState()
    val isLoading by capsuleViewModel.isLoading.collectAsState()
    val error by capsuleViewModel.error.collectAsState()
    val saveSuccess by capsuleViewModel.saveSuccess.collectAsState()

    // 🔹 NUEVO: Observamos los nombres cargados
    val contributorNames by capsuleViewModel.contributorNames.collectAsState()

    val context = LocalContext.current
    val currentUserId = remember { AuthRepository().getCurrentUser()?.uid }

    // Fullscreen viewer state
    var viewerUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var viewerIndex by remember { mutableStateOf(0) }
    var showViewer by remember { mutableStateOf(false) }

    LaunchedEffect(capsuleId) {
        capsuleViewModel.loadCapsuleById(capsuleId)
    }

    val savedMsg = stringResource(R.string.msg_contribution_saved)
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) Toast.makeText(context, savedMsg, Toast.LENGTH_SHORT).show()
    }

    DisposableEffect(Unit) {
        onDispose { capsuleViewModel.clearSelectedCapsule() }
    }

    // ---- Paleta (mismo enfoque que Settings) ----
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

    AppBackground(type = BackgroundType.GRADIENT, isDark = isDark) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                text = capsule?.title ?: "",
                                color = gold,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
                when {
                    isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = gold)
                    }

                    error != null -> Surface(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopCenter),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = error ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    capsule != null -> CapsuleDetailContent(
                        capsule = capsule!!,
                        viewModel = capsuleViewModel,
                        contributorNames = contributorNames, // 🔹 Pasamos el mapa
                        context = context,
                        currentUserId = currentUserId,
                        gold = gold,
                        titleGold = titleGold,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        cardBrush = cardBrush,
                        cardBorder = cardBorder,
                        onImageClick = { urls, idx ->
                            viewerUrls = urls
                            viewerIndex = idx
                            showViewer = true
                        }
                    )
                }

                if (showViewer && viewerUrls.isNotEmpty()) {
                    FullscreenImageViewer(
                        urls = viewerUrls,
                        startIndex = viewerIndex,
                        gold = gold,
                        onDismiss = { showViewer = false }
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
    contributorNames: Map<String, String>, // 🔹 Recibimos el mapa
    context: Context,
    currentUserId: String?,
    gold: Color,
    titleGold: Color,
    textPrimary: Color,
    textSecondary: Color,
    cardBrush: Brush,
    cardBorder: Color,
    onImageClick: (List<String>, Int) -> Unit
) {
    val isOwner = currentUserId != null && currentUserId == capsule.ownerId

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1) CÓDIGO INVITACIÓN (solo owner + compartida + editable + NO abierta)
        if (isOwner && capsule.isShared && capsule.isEditable() && !capsule.isOpenable()) {
            item {
                SectionCardGradient(stringResource(R.string.section_invitation), gold, titleGold, cardBrush, cardBorder) {
                    Text(
                        text = stringResource(R.string.lbl_invite_code),
                        color = textSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, gold.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(cardBrush)
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

                            val copyMsg = stringResource(R.string.msg_code_copied)
                            val labelCode = stringResource(R.string.lbl_capsule_code_clip)
                            IconButton(onClick = {
                                val clipboard =
                                    context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = ClipData.newPlainText(labelCode, capsule.inviteCode)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, copyMsg, Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = stringResource(R.string.copy), tint = gold)
                            }
                        }
                    }
                }
            }
        }

        // 2) CABECERA DE ESTADO
        item {
            val openDate = capsule.openDate.toDate()
            val stateTitle = if (capsule.isOpenable()) stringResource(R.string.status_open) else stringResource(R.string.status_closed)
            val stateIcon = if (capsule.isOpenable()) Icons.Default.LockOpen else Icons.Default.Lock
            val formattedDate = DateFormat.getDateTimeInstance().format(openDate)

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = Color.Transparent,
                shadowElevation = 10.dp,
                border = BorderStroke(1.dp, gold.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardBrush)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, gold.copy(alpha = 0.22f))
                    ) {
                        Box(
                            Modifier
                                .background(cardBrush)
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
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
                            text = stringResource(R.string.fmt_opens_at, formattedDate),
                            color = textSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // 3) ABIERTA -> SECCIONES POR USUARIO
        if (capsule.isOpenable()) {
            val entries = capsule.contributions.toList()

            if (entries.isEmpty()) {
                item {
                    SectionCardGradient(stringResource(R.string.section_content), gold, titleGold, cardBrush, cardBorder) {
                        Text(
                            text = stringResource(R.string.msg_no_contributions),
                            color = textSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                items(entries.size) { idx ->
                    val (userId, data) = entries[idx]
                    val userText = data["text"] as? String ?: ""
                    val images = data["images"] as? List<String> ?: emptyList()

                    // 🔹 Obtenemos el nombre del mapa, o fallback al ID recortado
                    val displayName = contributorNames[userId] ?: userId.take(10) + "…"

                    ContributionSection(
                        userName = displayName, // 🔹 Usamos el nombre
                        isCreator = (userId == capsule.creatorId),
                        text = userText,
                        images = images,
                        gold = gold,
                        titleGold = titleGold,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        cardBrush = cardBrush,
                        cardBorder = cardBorder,
                        onImageClick = { imageIndex -> onImageClick(images, imageIndex) }
                    )
                }
            }
        } else {
            // 4) CERRADA -> FORMULARIO SI EDITABLE
            item {
                if (capsule.isEditable()) {
                    val deadline = capsule.editDeadline.toDate()
                    val dateStr = DateFormat.getDateTimeInstance().format(deadline)

                    SectionCardGradient(stringResource(R.string.section_your_contribution), gold, titleGold, cardBrush, cardBorder) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = gold)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.fmt_deadline_warning, dateStr),
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
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = stringResource(R.string.msg_period_ended),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        item {
            val allImages = buildList {
                addAll(capsule.images)
                capsule.contributions.values.forEach { data ->
                    val imgs = data["images"] as? List<String> ?: emptyList()
                    addAll(imgs)
                }
            }.distinct()

            SectionCardGradient(stringResource(R.string.section_actions), gold, titleGold, cardBrush, cardBorder) {

                if (capsule.isOpenable() && allImages.isNotEmpty()) {
                    Button(
                        onClick = {
                            downloadAllCapsuleImages(
                                context = context,
                                capsuleTitle = capsule.title,
                                urls = allImages
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = gold,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(stringResource(R.string.btn_download_all_photos))
                    }

                    Spacer(Modifier.height(12.dp))
                }

                val msgLeft = stringResource(R.string.msg_capsule_left)
                Button(
                    onClick = {
                        viewModel.leaveCapsule(capsule.id)
                        Toast.makeText(context, msgLeft, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = gold,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(stringResource(R.string.btn_delete_capsule_action))
                }
            }
        }
    }
}

@Composable
private fun ContributionSection(
    userName: String, // 🔹 Cambiado userId -> userName
    isCreator: Boolean,
    text: String,
    images: List<String>,
    gold: Color,
    titleGold: Color,
    textPrimary: Color,
    textSecondary: Color,
    cardBrush: Brush,
    cardBorder: Color,
    onImageClick: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBrush)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, gold.copy(alpha = 0.20f))
                ) {
                    Box(
                        Modifier
                            .background(cardBrush)
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = gold)
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isCreator) stringResource(R.string.role_creator) else stringResource(R.string.role_participant),
                        color = titleGold,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = userName, // 🔹 Mostramos el nombre real
                        color = textSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (images.isNotEmpty()) {
                ImageCarousel(urls = images, gold = gold, onImageClick = onImageClick)
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, gold.copy(alpha = 0.12f))
                ) {
                    Text(
                        text = stringResource(R.string.lbl_no_images),
                        color = textSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardBrush)
                            .padding(12.dp)
                    )
                }
            }

            if (text.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, gold.copy(alpha = 0.18f))
                ) {
                    Text(
                        text = text,
                        color = textPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardBrush)
                            .padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.lbl_no_text),
                    color = textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.alpha(0.9f)
                )
            }
        }
    }
}

@Composable
private fun ImageCarousel(
    urls: List<String>,
    gold: Color,
    onImageClick: (Int) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(urls) { index, url ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, gold.copy(alpha = 0.20f)),
                modifier = Modifier.clickable { onImageClick(index) }
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
private fun FullscreenImageViewer(
    urls: List<String>,
    startIndex: Int,
    gold: Color,
    onDismiss: () -> Unit
) {
    val listState = androidx.compose.foundation.lazy.rememberLazyListState(
        initialFirstVisibleItemIndex = startIndex
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(urls) { _, url ->
                Box(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .fillParentMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF111111).copy(alpha = 0.75f),
                border = BorderStroke(1.dp, gold.copy(alpha = 0.35f))
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close), tint = gold)
                }
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
        label = { Text(stringResource(R.string.hint_write_future)) },
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
            unfocusedTextColor = textPrimary,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
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
            containerColor = Color.Transparent,
            contentColor = textPrimary
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = gold)
        Spacer(modifier = Modifier.width(10.dp))
        Text(stringResource(R.string.btn_attach_images), fontWeight = FontWeight.SemiBold)
    }

    if (selectedImageUris.isNotEmpty()) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            itemsIndexed(selectedImageUris) { _, uri ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.Transparent,
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
            text = stringResource(R.string.desc_add_images_too),
            color = textSecondary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.alpha(0.9f)
        )
    }
//
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
            containerColor = Color.Transparent,
            contentColor = textPrimary,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = textPrimary.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, gold.copy(alpha = 0.45f))
    ) {
        Text(stringResource(R.string.btn_save_part), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SectionCardGradient(
    title: String,
    gold: Color,
    titleGold: Color,
    cardBrush: Brush,
    cardBorder: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBrush)
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

private fun downloadAllCapsuleImages(
    context: Context,
    capsuleTitle: String,
    urls: List<String>
) {
    if (urls.isEmpty()) {
        val msg = context.getString(R.string.msg_no_images_to_download)
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        return
    }

    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    val safeTitle = capsuleTitle
        .trim()
        .take(24)
        .replace(Regex("[^a-zA-Z0-9_\\- ]"), "")
        .replace(" ", "_")
        .ifBlank { "capsule" }

    urls.distinct().forEachIndexed { index, url ->
        runCatching {
            val title = context.getString(R.string.download_notification_title, safeTitle)
            val desc = context.getString(R.string.download_notification_desc, index + 1, urls.size)

            val req = DownloadManager.Request(Uri.parse(url))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setTitle(title)
                .setDescription(desc)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "FutureMe_${safeTitle}_${index + 1}.jpg"
                )

            dm.enqueue(req)
        }
    }

    val msgStarted = context.getString(R.string.msg_download_started, urls.size)
    Toast.makeText(context, msgStarted, Toast.LENGTH_SHORT).show()
}