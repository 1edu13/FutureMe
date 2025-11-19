package com.example.futureme.ui.capsule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.futureme.data.model.Capsule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapsuleDetailScreen(
    capsuleId: String?,
    viewModel: CapsuleViewModel,
    onNavigateBack: () -> Unit
) {
    val capsule by viewModel.selectedCapsule.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(capsuleId) {
        if (capsuleId != null) {
            viewModel.loadCapsuleById(capsuleId)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearSelectedCapsule()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(capsule?.title ?: "Cápsula") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (error != null) {
                Text(text = error ?: "Ha ocurrido un error", color = MaterialTheme.colorScheme.error)
            } else if (capsule != null) {
                 CapsuleContent(capsule = capsule!!)
            }
        }
    }
}

@Composable
fun CapsuleContent(capsule: Capsule) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = capsule.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = capsule.text, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(24.dp))

        if (capsule.imageUrls.isNotEmpty()) {
            Text("Imágenes Adjuntas", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(capsule.imageUrls) { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Imagen de la cápsula",
                        modifier = Modifier.height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}