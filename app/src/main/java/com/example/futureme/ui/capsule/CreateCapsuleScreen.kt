package com.example.futureme.ui.capsule

import android.net.Uri
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
        if (saveSuccess) {
            onNavigateBack()
        }
    }

    CreateCapsuleScreenContent(
        isLoading = isLoading,
        error = error,
        onSave = { title, text, openDateTime ->
            capsuleViewModel.saveCapsule(title, text, openDateTime)
        },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCapsuleScreenContent(
    isLoading: Boolean,
    error: String?,
    onSave: (String, String, Calendar) -> Unit,
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var openDate by remember { mutableStateOf<Calendar?>(null) }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> selectedImageUris = uris }
    )

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(is24Hour = true)
    val dateTimeFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val openDateText = remember(openDate) {
        openDate?.let { dateTimeFormatter.format(it.time) } ?: ""
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) {
            showDatePicker = true
        }
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
            modifier = Modifier.padding(innerPadding).padding(16.dp).fillMaxSize(),
        ) {
            OutlinedTextField(
                value = title, onValueChange = { title = it }, label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(), enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = openDateText, onValueChange = {}, label = { Text("Fecha y hora de apertura") },
                readOnly = true, interactionSource = interactionSource,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = text, onValueChange = { text = it }, label = { Text("Contenido de tu cápsula") },
                modifier = Modifier.fillMaxWidth().weight(1f), enabled = !isLoading, minLines = 5
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { 
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            .build()
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
                            model = uri, contentDescription = null,
                            modifier = Modifier.size(80.dp).padding(end = 4.dp), contentScale = ContentScale.Crop
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
                        openDate?.let {
                            onSave(title, text, it)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && title.isNotBlank() && text.isNotBlank() && openDate != null
                ) {
                    Text("Guardar Cápsula")
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { 
                    datePickerState.selectedDateMillis?.let { utcMillis ->
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
                    showDatePicker = false
                    showTimePicker = true 
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Selecciona la hora") },
            text = { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { TimePicker(state = timePickerState) } },
            confirmButton = {
                TextButton(onClick = {
                    val calendarToUpdate = openDate ?: Calendar.getInstance()
                    calendarToUpdate.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    calendarToUpdate.set(Calendar.MINUTE, timePickerState.minute)
                    
                    val finalCalendar = Calendar.getInstance()
                    finalCalendar.time = calendarToUpdate.time
                    openDate = finalCalendar
                    
                    showTimePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") } }
        )
    }
}
