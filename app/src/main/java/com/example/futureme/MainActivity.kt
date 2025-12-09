package com.example.futureme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.futureme.data.model.Capsule
import com.example.futureme.ui.auth.AuthViewModel
import com.example.futureme.ui.auth.LoginScreen
import com.example.futureme.ui.capsule.*
import com.example.futureme.ui.navigation.Screen
import com.example.futureme.ui.theme.FutureMeTheme
import java.text.DateFormat

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val capsuleViewModel: CapsuleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FutureMeTheme {
                AppNavHost(authViewModel = authViewModel, capsuleViewModel = capsuleViewModel)
            }
        }
    }
}

@Composable
fun AppNavHost(
    authViewModel: AuthViewModel,
    capsuleViewModel: CapsuleViewModel
) {
    val navController = rememberNavController()
    val user by authViewModel.user.collectAsState()

    // --- Control automático entre Login y Home ---
    LaunchedEffect(user) {
        val target = if (user == null) Screen.Login.route else Screen.Home.route
        navController.navigate(target) {
            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {

        // LOGIN
        composable(Screen.Login.route) {
            LoginScreen(authViewModel = authViewModel)
        }

        // HOME
        composable(Screen.Home.route) {
            HomeScreen(
                authViewModel = authViewModel,
                capsuleViewModel = capsuleViewModel,
                onNavigateToCreate = { navController.navigate(Screen.CreateCapsule.route) },
                onJoinCapsule = { navController.navigate(Screen.JoinCapsule.route) },
                onCapsuleClick = { capsule ->
                    // 👉 SIEMPRE DEJAMOS ENTRAR A DETALLES
                    navController.navigate(Screen.CapsuleDetail.createRoute(capsule.id))
                }
            )
        }

        // CREAR CÁPSULA
        composable(Screen.CreateCapsule.route) {
            CreateCapsuleScreen(
                capsuleViewModel = capsuleViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // UNIRSE A CÁPSULA
        composable(Screen.JoinCapsule.route) {
            JoinCapsuleScreen(
                capsuleViewModel = capsuleViewModel,
                onNavigateBack = { navController.popBackStack() },
                onJoined = { navController.navigate(Screen.Home.route) }
            )
        }

        // DETALLES DE CÁPSULA
        composable(
            route = Screen.CapsuleDetail.route,
            arguments = listOf(navArgument("capsuleId") { type = NavType.StringType })
        ) {
            val capsuleId = it.arguments?.getString("capsuleId") ?: return@composable
            CapsuleDetailScreen(
                capsuleViewModel = capsuleViewModel,
                capsuleId = capsuleId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    capsuleViewModel: CapsuleViewModel,
    onNavigateToCreate: () -> Unit,
    onJoinCapsule: () -> Unit,
    onCapsuleClick: (Capsule) -> Unit
) {
    val capsules by capsuleViewModel.capsules.collectAsState()
    val isLoading by capsuleViewModel.isLoading.collectAsState()
    val error by capsuleViewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        capsuleViewModel.loadCapsules()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Cápsulas") },
                actions = {
                    IconButton(onClick = { authViewModel.signOut() }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {

            // BOTÓN UNIRSE A CÁPSULA
            Button(
                onClick = onJoinCapsule,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Unirse a cápsula")
            }

            if (isLoading && capsules.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }

            if (capsules.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Aún no tienes cápsulas. ¡Crea una!")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(capsules) { capsule ->
                        CapsuleItem(capsule = capsule) {
                            onCapsuleClick(capsule)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CapsuleItem(capsule: Capsule, onClick: () -> Unit) {
    val isOpenable = capsule.isOpenable()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)              // <-- SIEMPRE CLICK
            .alpha(if (isOpenable) 1f else 0.6f),      // Solo cambia opacidad
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = capsule.title, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = capsule.text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            val openDate = capsule.openDate.toDate()
            Text(
                text = if (isOpenable) "¡Ya se puede abrir!" else "Se abre el: ${
                    DateFormat.getDateInstance(DateFormat.MEDIUM).format(openDate)
                }",
                style = MaterialTheme.typography.labelSmall,
                color = if (isOpenable) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
