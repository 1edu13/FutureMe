package com.example.futureme.ui.capsule

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

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
        onSave = { title, text, editDeadline, openDateTime, imageUris, context ->
            capsuleViewModel.saveCapsule(
                title = title,
                text = text,
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
    onSave: (String, String, Calendar, Calendar, List<Uri>, Context) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }

    // ✅ Nuevo: deadline para editar/unirse
    var editDeadline by remember { mutableStateOf<Calendar?>(null) }

    // Apertura
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Cápsula") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, enabled = !isLoading) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Switch(
                    checked = isShared,
                    onCheckedChange = { isShared = it },
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cápsula compartida")
            }

            if (isShared) {
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
                            text = "Código de invitación",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Se generará automáticamente al guardar la cápsula.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Nuevo: Deadline editar/unirse
            OutlinedTextField(
                value = editDeadlineText,
                onValueChange = {},
                label = { Text("Límite para editar / unirse") },
                readOnly = true,
                interactionSource = deadlineInteractionSource,
                trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Apertura
            OutlinedTextField(
                value = openDateText,
                onValueChange = {},
                label = { Text("Fecha y hora de apertura") },
                readOnly = true,
                interactionSource = openInteractionSource,
                trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Contenido de tu cápsula") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                minLines = 5,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
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

            error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        val d = editDeadline
                        val o = openDate
                        if (d != null && o != null) {
                            onSave(title, text, d, o, selectedImageUris, context)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading &&
                            title.isNotBlank() &&
                            text.isNotBlank() &&
                            editDeadline != null &&
                            openDate != null
                ) {
                    Text("Guardar Cápsula")
                }
            }
        }
    }

    // -------------------------
    // DEADLINE: DatePicker
    // -------------------------
    if (showDeadlineDatePicker) {
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

    // DEADLINE: TimePicker
    if (showDeadlineTimePicker) {
        AlertDialog(
            onDismissRequest = { showDeadlineTimePicker = false },
            title = { Text("Selecciona la hora (límite)") },
            text = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = deadlineTimePickerState)
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

    // -------------------------
    // OPEN DATE: DatePicker
    // -------------------------
    if (showOpenDatePicker) {
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

    // OPEN DATE: TimePicker
    if (showOpenTimePicker) {
        AlertDialog(
            onDismissRequest = { showOpenTimePicker = false },
            title = { Text("Selecciona la hora (apertura)") },
            text = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = openTimePickerState)
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
