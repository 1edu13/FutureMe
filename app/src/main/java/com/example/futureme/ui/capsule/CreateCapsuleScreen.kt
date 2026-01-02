package com.example.futureme.ui.capsule

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.futureme.R
import com.example.futureme.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCapsuleScreen(
    capsuleViewModel: CapsuleViewModel,
    onNavigateBack: () -> Unit,
    isDark: Boolean
) {
    val isLoading by capsuleViewModel.isLoading.collectAsState()
    val error by capsuleViewModel.error.collectAsState()
    val saveSuccess by capsuleViewModel.saveSuccess.collectAsState()

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) onNavigateBack()
    }

    CreateCapsuleScreenContent(
        isLoading = isLoading,
        error = error,
        onSave = { title, text, isShared, editDeadline, openDateTime, imageUris, context ->
            capsuleViewModel.saveCapsule(
                title = title,
                text = text,
                isShared = isShared,
                editDeadline = editDeadline,
                openDateTime = openDateTime,
                imageUris = imageUris,
                context = context
            )
        },
        onNavigateBack = onNavigateBack,
        isDark = isDark
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCapsuleScreenContent(
    isDark: Boolean,
    isLoading: Boolean,
    error: String?,
    onSave: (String, String, Boolean, Calendar, Calendar, List<Uri>, Context) -> Unit,
    onNavigateBack: () -> Unit
) {

    val gold = OroAmbar
    val titleGold = if (isDark) gold else AzulProfundo
    val textPrimary = if (isDark) BlancoAzulado else AzulProfundo
    val textSecondary = if (isDark) GrisAzulado else ClaroBorde

    // “Seda/cristal” para cards (igual que Settings)
    val cardBrush = if (isDark) {
        Brush.verticalGradient(listOf(AzulSuperficie, AzulProfundo))
    } else {
        Brush.verticalGradient(listOf(ClaroBase, ClaroSuave, ClaroPrincipal))
    }
    val cardBorder = if (isDark) gold.copy(alpha = 0.30f) else ClaroBorde.copy(alpha = 0.80f)

    // Tema para dialogs (DatePicker/TimePicker) -> mismo comportamiento, solo colores
    val pickerScheme = if (isDark) {
        darkColorScheme(
            primary = gold,
            secondary = gold,
            tertiary = gold,
            onPrimary = Color.Black,
            background = AzulProfundo,
            surface = AzulSuperficie,
            onSurface = textPrimary,
            onBackground = textPrimary
        )
    } else {
        lightColorScheme(
            primary = gold,
            secondary = gold,
            tertiary = gold,
            onPrimary = Color.Black,
            background = ClaroPrincipal,
            surface = ClaroSuave,
            onSurface = textPrimary,
            onBackground = textPrimary
        )
    }

    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }

    var editDeadline by remember { mutableStateOf<Calendar?>(null) }
    var openDate by remember { mutableStateOf<Calendar?>(null) }

    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isShared by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            uris.forEach { uri -> Log.d("PhotoPicker", "URI seleccionada: $uri") }
            selectedImageUris = uris
        }
    )

    // ---- Pickers Deadline ----
    var showDeadlineDatePicker by remember { mutableStateOf(false) }
    var showDeadlineTimePicker by remember { mutableStateOf(false) }
    val deadlineDatePickerState = rememberDatePickerState()
    val deadlineTimePickerState = rememberTimePickerState(is24Hour = true)

    // ---- Pickers OpenDate ----
    var showOpenDatePicker by remember { mutableStateOf(false) }
    var showOpenTimePicker by remember { mutableStateOf(false) }
    val openDatePickerState = rememberDatePickerState()
    val openTimePickerState = rememberTimePickerState(is24Hour = true)

    val dateTimeFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    val editDeadlineText = remember(editDeadline) {
        editDeadline?.let { dateTimeFormatter.format(it.time) } ?: ""
    }
    val openDateText = remember(openDate) {
        openDate?.let { dateTimeFormatter.format(it.time) } ?: ""
    }

    // (Se mantiene tu comportamiento: abrir el datePicker al pulsar)
    val deadlineInteractionSource = remember { MutableInteractionSource() }
    val deadlinePressed by deadlineInteractionSource.collectIsPressedAsState()
    LaunchedEffect(deadlinePressed) {
        if (deadlinePressed) showDeadlineDatePicker = true
    }

    val openInteractionSource = remember { MutableInteractionSource() }
    val openPressed by openInteractionSource.collectIsPressedAsState()
    LaunchedEffect(openPressed) {
        if (openPressed) showOpenDatePicker = true
    }

    val canSave =
        !isLoading &&
                title.isNotBlank() &&
                text.isNotBlank() &&
                editDeadline != null &&
                openDate != null

    val scrollState = rememberScrollState()

    // ✅ Fondo: solo degradado (como Settings), sin imagen
    AppBackground(type = BackgroundType.GRADIENT, isDark = isDark) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                stringResource(R.string.create_capsule_title),
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
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                    Divider(color = gold.copy(alpha = 0.18f), thickness = 1.dp)
                }
            }
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                SectionCard(
                    title = stringResource(R.string.section_details),
                    gold = gold,
                    titleGold = titleGold,
                    cardBrush = cardBrush,
                    cardBorder = cardBorder
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(stringResource(R.string.lbl_title)) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        colors = goldFieldColors(gold, textPrimary, textSecondary)
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text(stringResource(R.string.lbl_content)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp),
                        minLines = 6,
                        enabled = !isLoading,
                        colors = goldFieldColors(gold, textPrimary, textSecondary)
                    )
                }

                SectionCard(
                    title = stringResource(R.string.section_privacy),
                    gold = gold,
                    titleGold = titleGold,
                    cardBrush = cardBrush,
                    cardBorder = cardBorder
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.lbl_shared_capsule),
                                color = textPrimary,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(R.string.desc_shared_capsule),
                                color = textSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Switch(
                            checked = isShared,
                            onCheckedChange = { isShared = it },
                            enabled = !isLoading,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = gold,
                                checkedTrackColor = gold.copy(alpha = 0.5f),
                                uncheckedThumbColor = textPrimary.copy(alpha = 0.6f),
                                uncheckedTrackColor = if (isDark) AzulSuperficie else ClaroSuave
                            )
                        )
                    }

                    if (isShared) {
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            color = Color.Transparent,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, gold.copy(alpha = 0.20f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                Modifier
                                    .background(cardBrush)
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.section_invites),
                                    color = gold,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = stringResource(R.string.desc_invites),
                                    color = textSecondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                SectionCard(
                    title = stringResource(R.string.section_dates),
                    gold = gold,
                    titleGold = titleGold,
                    cardBrush = cardBrush,
                    cardBorder = cardBorder
                ) {
                    DateCard(
                        title = stringResource(R.string.lbl_deadline),
                        value = if (editDeadlineText.isEmpty()) stringResource(R.string.hint_select_deadline) else editDeadlineText,
                        iconTint = gold,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        enabled = !isLoading,
                        cardBrush = cardBrush,
                        borderColor = gold.copy(alpha = 0.22f),
                        onClick = { showDeadlineDatePicker = true }
                    )

                    Spacer(Modifier.height(10.dp))

                    DateCard(
                        title = stringResource(R.string.lbl_opening_date),
                        value = if (openDateText.isEmpty()) stringResource(R.string.hint_select_opening) else openDateText,
                        iconTint = gold,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        enabled = !isLoading,
                        cardBrush = cardBrush,
                        borderColor = gold.copy(alpha = 0.22f),
                        onClick = { showOpenDatePicker = true }
                    )
                }

                SectionCard(
                    title = stringResource(R.string.section_images),
                    gold = gold,
                    titleGold = titleGold,
                    cardBrush = cardBrush,
                    cardBorder = cardBorder
                ) {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        border = BorderStroke(1.dp, gold.copy(alpha = 0.35f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = textPrimary,
                            containerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = gold)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(stringResource(R.string.btn_attach_images), fontWeight = FontWeight.SemiBold)
                    }

                    if (selectedImageUris.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(selectedImageUris) { uri ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, gold.copy(alpha = 0.18f)),
                                    color = Color.Transparent
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        modifier = Modifier.size(84.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.desc_images_optional),
                            color = textSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                error?.let {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (isLoading) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = gold)
                    }
                } else {
                    Button(
                        onClick = {
                            val d = editDeadline
                            val o = openDate
                            if (d != null && o != null) {
                                onSave(title, text, isShared, d, o, selectedImageUris, context)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        enabled = canSave,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = textPrimary,
                            disabledContainerColor = Color.Transparent,
                            disabledContentColor = textPrimary.copy(alpha = 0.35f)
                        ),
                        border = BorderStroke(1.dp, gold.copy(alpha = if (canSave) 0.55f else 0.22f))
                    ) {
                        Text(stringResource(R.string.btn_save_capsule), fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(6.dp))
            }
        }
    }

    // -------------------------
    // DEADLINE: DatePicker
    // -------------------------
    if (showDeadlineDatePicker) {
        MaterialTheme(colorScheme = pickerScheme) {
            DatePickerDialog(
                onDismissRequest = { showDeadlineDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            deadlineDatePickerState.selectedDateMillis?.let { utcMillis ->
                                val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                                utcCal.timeInMillis = utcMillis

                                val year = utcCal.get(Calendar.YEAR)
                                val month = utcCal.get(Calendar.MONTH)
                                val day = utcCal.get(Calendar.DAY_OF_MONTH)

                                val localCal = Calendar.getInstance()
                                localCal.clear()
                                localCal.set(year, month, day)

                                editDeadline = localCal
                            }
                            showDeadlineDatePicker = false
                            showDeadlineTimePicker = true
                        }
                    ) { Text(stringResource(R.string.accept)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeadlineDatePicker = false }) { Text(stringResource(R.string.cancel)) }
                }
            ) {
                DatePicker(state = deadlineDatePickerState)
            }
        }
    }

    // DEADLINE: TimePicker
    if (showDeadlineTimePicker) {
        MaterialTheme(colorScheme = pickerScheme) {
            AlertDialog(
                onDismissRequest = { showDeadlineTimePicker = false },
                containerColor = if (isDark) AzulSuperficie else ClaroSuave,
                title = {
                    Text(
                        stringResource(R.string.dialog_time_deadline),
                        color = gold,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                text = {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, gold.copy(alpha = 0.20f))
                    ) {
                        Box(
                            Modifier
                                .background(cardBrush)
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            TimePicker(state = deadlineTimePickerState)
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val calendarToUpdate = editDeadline ?: Calendar.getInstance()
                            calendarToUpdate.set(Calendar.HOUR_OF_DAY, deadlineTimePickerState.hour)
                            calendarToUpdate.set(Calendar.MINUTE, deadlineTimePickerState.minute)

                            val finalCalendar = Calendar.getInstance()
                            finalCalendar.time = calendarToUpdate.time
                            editDeadline = finalCalendar

                            showDeadlineTimePicker = false
                        }
                    ) { Text(stringResource(R.string.accept)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeadlineTimePicker = false }) { Text(stringResource(R.string.cancel)) }
                }
            )
        }
    }

    // -------------------------
    // OPEN DATE: DatePicker
    // -------------------------
    if (showOpenDatePicker) {
        MaterialTheme(colorScheme = pickerScheme) {
            DatePickerDialog(
                onDismissRequest = { showOpenDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            openDatePickerState.selectedDateMillis?.let { utcMillis ->
                                val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                                utcCal.timeInMillis = utcMillis

                                val year = utcCal.get(Calendar.YEAR)
                                val month = utcCal.get(Calendar.MONTH)
                                val day = utcCal.get(Calendar.DAY_OF_MONTH)

                                val localCal = Calendar.getInstance()
                                localCal.clear()
                                localCal.set(year, month, day)

                                openDate = localCal
                            }
                            showOpenDatePicker = false
                            showOpenTimePicker = true
                        }
                    ) { Text(stringResource(R.string.accept)) }
                },
                dismissButton = {
                    TextButton(onClick = { showOpenDatePicker = false }) { Text(stringResource(R.string.cancel)) }
                }
            ) {
                DatePicker(state = openDatePickerState)
            }
        }
    }

    // OPEN DATE: TimePicker
    if (showOpenTimePicker) {
        MaterialTheme(colorScheme = pickerScheme) {
            AlertDialog(
                onDismissRequest = { showOpenTimePicker = false },
                containerColor = if (isDark) AzulSuperficie else ClaroSuave,
                title = {
                    Text(
                        stringResource(R.string.dialog_time_opening),
                        color = gold,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                text = {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, gold.copy(alpha = 0.20f))
                    ) {
                        Box(
                            Modifier
                                .background(cardBrush)
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            TimePicker(state = openTimePickerState)
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val calendarToUpdate = openDate ?: Calendar.getInstance()
                            calendarToUpdate.set(Calendar.HOUR_OF_DAY, openTimePickerState.hour)
                            calendarToUpdate.set(Calendar.MINUTE, openTimePickerState.minute)

                            val finalCalendar = Calendar.getInstance()
                            finalCalendar.time = calendarToUpdate.time
                            openDate = finalCalendar

                            showOpenTimePicker = false
                        }
                    ) { Text(stringResource(R.string.accept)) }
                },
                dismissButton = {
                    TextButton(onClick = { showOpenTimePicker = false }) { Text(stringResource(R.string.cancel)) }
                }
            )
        }
    }
}

@Composable
private fun SectionCard(
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

@Composable
private fun DateCard(
    title: String,
    value: String,
    iconTint: Color,
    textPrimary: Color,
    textSecondary: Color,
    enabled: Boolean,
    cardBrush: Brush,
    borderColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBrush)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null, tint = iconTint)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = textSecondary, style = MaterialTheme.typography.labelLarge)
                Text(
                    value,
                    color = textPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun goldFieldColors(
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