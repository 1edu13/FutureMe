package com.example.futureme.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.futureme.data.model.Capsule
import com.example.futureme.ui.auth.AuthViewModel
import com.example.futureme.ui.capsule.CapsuleViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    capsuleViewModel: CapsuleViewModel,
    onNavigateToCreate: () -> Unit,
    onCapsuleClick: (String) -> Unit
) {
    val capsules by capsuleViewModel.capsules.collectAsState()
    val isLoading by capsuleViewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Cápsulas") },
                actions = {
                    IconButton(onClick = { authViewModel.signOut() }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreate) {
                Icon(Icons.Default.Add, contentDescription = "Crear nueva cápsula")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (capsules.isEmpty()) {
                Text("Todavía no has creado ninguna cápsula.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(capsules) { capsule ->
                        CapsuleCard(
                            capsule = capsule,
                            onClick = {
                                val now = Timestamp.now()
                                if (capsule.openDate != null && capsule.openDate.seconds <= now.seconds) {
                                    onCapsuleClick(capsule.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CapsuleCard(capsule: Capsule, onClick: () -> Unit) {
    val now = Timestamp.now()
    val canBeOpened = capsule.openDate != null && capsule.openDate.seconds <= now.seconds
    
    val cardColors = if (canBeOpened) {
        CardDefaults.elevatedCardColors()
    } else {
        CardDefaults.cardColors()
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = canBeOpened, onClick = onClick),
        elevation = if (canBeOpened) CardDefaults.elevatedCardElevation() else CardDefaults.cardElevation(),
        colors = cardColors
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = capsule.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            capsule.openDate?.let {
                val ldt = LocalDateTime.ofInstant(Instant.ofEpochSecond(it.seconds), ZoneId.systemDefault())
                val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
                Text("Se abrirá el: ${ldt.format(formatter)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Countdown(openDate = capsule.openDate, canBeOpened = canBeOpened)
        }
    }
}

@Composable
fun Countdown(openDate: Timestamp?, canBeOpened: Boolean) {
    if (canBeOpened) {
        Text("¡Ya puedes abrirla!", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        return
    }

    var timeRemaining by remember { mutableStateOf("") }

    LaunchedEffect(openDate) {
        while (true) {
            val nowMillis = System.currentTimeMillis()
            val openMillis = (openDate?.seconds ?: 0) * 1000
            val diff = openMillis - nowMillis

            if (diff > 0) {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60
                
                timeRemaining = String.format("%d D, %02d H, %02d M, %02d S", days, hours, minutes, seconds)
            } else {
                timeRemaining = "¡Listo para abrir!"
            }
            delay(1000)
        }
    }

    Text(text = timeRemaining, style = MaterialTheme.typography.bodyMedium)
}