package com.example.futureme.ui.capsule

import android.content.Context
import androidx.compose.ui.graphics.Color
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.futureme.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCapsuleScreen(
    capsuleViewModel: CapsuleViewModel,
    onNavigateBack: () -> Unit
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
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCapsuleScreenContent(
    isLoading: Boolean,
    error: String?,
    onSave: (String, String, Boolean, Calendar, Calendar, List<Uri>, Context) -> Unit,
    onNavigateBack: () -> Unit
) {
    // ---- Paleta ----
    val gold = Color(0xFFD4AF37)
    val titleGold = Color(0xFFC9A84D)
    val textPrimary = Color(0xFFEDEDED)
    val textSecondary = textPrimary.copy(alpha = 0.75f)
    val bg = Color(0xFF0B0B0B)
    val cardBg = Color(0xFF0E0E0E)
    val toolbarBg = Color(0xFF161616)

    // Tema oscuro para dialogs (DatePicker/TimePicker)
    val darkPickerColors = darkColorScheme(
        primary = gold,
        secondary = gold,
        tertiary = gold,
        onPrimary = Color.Black,
        background = Color(0xFF101010),
        surface = Color(0xFF121212),
        onSurface = textPrimary,
        onBackground = textPrimary
    )

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
        editDeadline?.let { dateTimeFormatter.format(it.time) } ?: "Seleccionar límite"
    }
    val openDateText = remember(openDate) {
        openDate?.let { dateTimeFormatter.format(it.time) } ?: "Seleccionar apertura"
    }

    // Abrir DatePicker al pulsar el field (deadline)
    val deadlineInteractionSource = remember { MutableInteractionSource() }
    val deadlinePressed by deadlineInteractionSource.collectIsPressedAsState()
    LaunchedEffect(deadlinePressed) {
        if (deadlinePressed) showDeadlineDatePicker = true
    }

    // Abrir DatePicker al pulsar el field (open date)
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

    Scaffold(
        containerColor = bg,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "Crear cápsula",
                            color = titleGold,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack, enabled = !isLoading) {
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
            // Fondo como el login (si lo tienes)
            Image(
                painter = painterResource(id = R.drawable.login_bg_dark),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            // Overlay oscuro
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.70f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)   // ✅ ahora baja sí o sí
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                SectionCard(title = "Detalles", gold = gold, titleGold = titleGold, cardBg = cardBg) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        colors = goldFieldColors(gold, textPrimary)
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("Contenido") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp),
                        minLines = 6,
                        enabled = !isLoading,
                        colors = goldFieldColors(gold, textPrimary)
                    )
                }

                SectionCard(title = "Privacidad", gold = gold, titleGold = titleGold, cardBg = cardBg) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cápsula compartida",
                                color = textPrimary,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Permite invitar usuarios (cuando lo habilites).",
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
                                checkedTrackColor = gold.copy(alpha = 0.35f),
                                uncheckedThumbColor = textPrimary.copy(alpha = 0.6f),
                                uncheckedTrackColor = Color(0xFF1B1B1B)
                            )
                        )
                    }

                    if (isShared) {
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            color = Color(0xFF121212),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, gold.copy(alpha = 0.20f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text(
                                    text = "Invitaciones",
                                    color = titleGold,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "Se configurará al guardar la cápsula.",
                                    color = textSecondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                SectionCard(title = "Fechas", gold = gold, titleGold = titleGold, cardBg = cardBg) {
                    // Tarjeta clickable: Deadline
                    DateCard(
                        title = "Límite para editar / unirse",
                        value = editDeadlineText,
                        iconTint = gold,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        enabled = !isLoading,
                        onClick = { showDeadlineDatePicker = true }
                    )

                    Spacer(Modifier.height(10.dp))

                    // Tarjeta clickable: Open
                    DateCard(
                        title = "Fecha y hora de apertura",
                        value = openDateText,
                        iconTint = gold,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        enabled = !isLoading,
                        onClick = { showOpenDatePicker = true }
                    )
                }

                SectionCard(title = "Imágenes", gold = gold, titleGold = titleGold, cardBg = cardBg) {
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
                            containerColor = Color(0xFF121212)
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = gold)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Adjuntar imágenes", fontWeight = FontWeight.SemiBold)
                    }

                    if (selectedImageUris.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(selectedImageUris) { uri ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, gold.copy(alpha = 0.18f)),
                                    color = Color(0xFF111111)
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
                            text = "Opcional: puedes añadir imágenes a tu cápsula.",
                            color = textSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                error?.let {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF1A0E0E),
                        border = BorderStroke(1.dp, Color(0xFFB24A4A).copy(alpha = 0.55f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = it,
                            color = textPrimary,
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
                            containerColor = if (canSave) Color(0xFF121212) else Color(0xFF101010),
                            contentColor = textPrimary,
                            disabledContainerColor = Color(0xFF101010),
                            disabledContentColor = textPrimary.copy(alpha = 0.35f)
                        ),
                        border = BorderStroke(1.dp, gold.copy(alpha = if (canSave) 0.45f else 0.18f))
                    ) {
                        Text("Guardar cápsula", fontWeight = FontWeight.SemiBold)
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
        MaterialTheme(colorScheme = darkPickerColors) {
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
                    ) { Text("Aceptar") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeadlineDatePicker = false }) { Text("Cancelar") }
                }
            ) {
                DatePicker(state = deadlineDatePickerState)
            }
        }
    }

    // DEADLINE: TimePicker
    if (showDeadlineTimePicker) {
        MaterialTheme(colorScheme = darkPickerColors) {
            AlertDialog(
                onDismissRequest = { showDeadlineTimePicker = false },
                containerColor = Color(0xFF121212),
                title = { Text("Selecciona la hora (límite)", color = titleGold, fontWeight = FontWeight.SemiBold) },
                text = {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF101010),
                        border = BorderStroke(1.dp, gold.copy(alpha = 0.20f))
                    ) {
                        Box(Modifier.padding(14.dp), contentAlignment = Alignment.Center) {
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
                    ) { Text("Aceptar") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeadlineTimePicker = false }) { Text("Cancelar") }
                }
            )
        }
    }

    // -------------------------
    // OPEN DATE: DatePicker
    // -------------------------
    if (showOpenDatePicker) {
        MaterialTheme(colorScheme = darkPickerColors) {
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
                    ) { Text("Aceptar") }
                },
                dismissButton = {
                    TextButton(onClick = { showOpenDatePicker = false }) { Text("Cancelar") }
                }
            ) {
                DatePicker(state = openDatePickerState)
            }
        }
    }

    // OPEN DATE: TimePicker
    if (showOpenTimePicker) {
        MaterialTheme(colorScheme = darkPickerColors) {
            AlertDialog(
                onDismissRequest = { showOpenTimePicker = false },
                containerColor = Color(0xFF121212),
                title = { Text("Selecciona la hora (apertura)", color = titleGold, fontWeight = FontWeight.SemiBold) },
                text = {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF101010),
                        border = BorderStroke(1.dp, gold.copy(alpha = 0.20f))
                    ) {
                        Box(Modifier.padding(14.dp), contentAlignment = Alignment.Center) {
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
                    ) { Text("Aceptar") }
                },
                dismissButton = {
                    TextButton(onClick = { showOpenTimePicker = false }) { Text("Cancelar") }
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

@Composable
private fun DateCard(
    title: String,
    value: String,
    iconTint: Color,
    textPrimary: Color,
    textSecondary: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF121212),
        border = BorderStroke(1.dp, iconTint.copy(alpha = 0.22f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
private fun goldFieldColors(gold: Color, textPrimary: Color) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = gold,
    focusedLabelColor = gold,
    cursorColor = gold,
    unfocusedBorderColor = gold.copy(alpha = 0.25f),
    unfocusedLabelColor = textPrimary.copy(alpha = 0.75f),
    focusedTextColor = textPrimary,
    unfocusedTextColor = textPrimary
)
