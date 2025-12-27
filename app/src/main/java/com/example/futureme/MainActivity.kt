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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.futureme.data.model.Capsule
import com.example.futureme.ui.auth.AuthViewModel
import com.example.futureme.ui.auth.LoginScreen
import com.example.futureme.ui.capsule.CapsuleDetailScreen
import com.example.futureme.ui.capsule.CapsuleViewModel
import com.example.futureme.ui.capsule.CreateCapsuleScreen
import com.example.futureme.ui.capsule.JoinCapsuleScreen
import com.example.futureme.ui.navigation.Screen
import com.example.futureme.ui.theme.FutureMeTheme
import kotlinx.coroutines.launch
import java.text.DateFormat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val capsuleViewModel: CapsuleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
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

    // Saber en qué ruta estamos
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Drawer
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // --- Control automático entre Login y MENÚ PRINCIPAL ---
    LaunchedEffect(user) {
        val target = if (user == null) Screen.Login.route else Screen.MainMenu.route
        navController.navigate(target) {
            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
            launchSingleTop = true
        }
    }

    // ✅ Solo mostramos drawer si NO estamos en login y hay usuario
    val showDrawer = user != null && currentRoute != Screen.Login.route

    if (showDrawer) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppDrawerContent(
                    userEmail = user?.email ?: "",
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        scope.launch { drawerState.close() }
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    },
                    onLogout = {
                        scope.launch { drawerState.close() }
                        authViewModel.signOut()
                    }
                )
            }
        ) {
            AppNavContent(
                navController = navController,
                authViewModel = authViewModel,
                capsuleViewModel = capsuleViewModel,
                userId = user?.uid,
                onOpenDrawer = { scope.launch { drawerState.open() } }
            )
        }
    } else {
        // Sin drawer (login)
        AppNavContent(
            navController = navController,
            authViewModel = authViewModel,
            capsuleViewModel = capsuleViewModel,
            userId = user?.uid,
            onOpenDrawer = null
        )
    }
}

@Composable
private fun AppNavContent(
    navController: androidx.navigation.NavHostController,
    authViewModel: AuthViewModel,
    capsuleViewModel: CapsuleViewModel,
    userId: String?,
    onOpenDrawer: (() -> Unit)?
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {

        // LOGIN
        composable(Screen.Login.route) {
            LoginScreen(authViewModel = authViewModel)
        }

        // ✅ MENÚ PRINCIPAL (nuevo)
        composable(Screen.MainMenu.route) {
            MainMenuScreen(
                onMenuClick = onOpenDrawer,
                onGoToCapsules = { navController.navigate(Screen.Home.route) },
                onGoToCreate = { navController.navigate(Screen.CreateCapsule.route) },
                onGoToJoin = { navController.navigate(Screen.JoinCapsule.route) },
                onGoToProfile = { navController.navigate(Screen.Profile.route) },
                onGoToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        // HOME (Mis cápsulas)
        composable(Screen.Home.route) {
            HomeScreen(
                userId = userId,
                onMenuClick = onOpenDrawer,
                capsuleViewModel = capsuleViewModel,
                onCapsuleClick = { capsule ->
                    navController.navigate(Screen.CapsuleDetail.createRoute(capsule.id))
                },
                onNavigateToCreate = { navController.navigate(Screen.CreateCapsule.route) },
                onJoinCapsule = { navController.navigate(Screen.JoinCapsule.route) }
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

        // PERFIL
        composable(Screen.Profile.route) {
            ProfileScreen(
                userEmail = authViewModel.user.collectAsState().value?.email ?: "",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // AJUSTES
        composable(Screen.Settings.route) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
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

@Composable
private fun AppDrawerContent(
    userEmail: String,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet {
        Spacer(Modifier.height(12.dp))
        Text(
            text = "FutureMe",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Text(
            text = userEmail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(12.dp))
        Divider()

        NavigationDrawerItem(
            label = { Text("Menú") },
            selected = currentRoute == Screen.MainMenu.route,
            onClick = { onNavigate(Screen.MainMenu.route) },
            icon = { Icon(Icons.Default.Menu, contentDescription = null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = { Text("Mis cápsulas") },
            selected = currentRoute == Screen.Home.route,
            onClick = { onNavigate(Screen.Home.route) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = { Text("Crear cápsula") },
            selected = currentRoute == Screen.CreateCapsule.route,
            onClick = { onNavigate(Screen.CreateCapsule.route) },
            icon = { Icon(Icons.Default.AddCircle, contentDescription = null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = { Text("Unirse a cápsula") },
            selected = currentRoute == Screen.JoinCapsule.route,
            onClick = { onNavigate(Screen.JoinCapsule.route) },
            icon = { Icon(Icons.Default.Link, contentDescription = null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        Spacer(Modifier.height(8.dp))
        Divider()
        Spacer(Modifier.height(8.dp))

        NavigationDrawerItem(
            label = { Text("Perfil") },
            selected = currentRoute == Screen.Profile.route,
            onClick = { onNavigate(Screen.Profile.route) },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = { Text("Ajustes") },
            selected = currentRoute == Screen.Settings.route,
            onClick = { onNavigate(Screen.Settings.route) },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        Spacer(Modifier.height(8.dp))
        Divider()
        Spacer(Modifier.height(8.dp))

        NavigationDrawerItem(
            label = { Text("Cerrar sesión") },
            selected = false,
            onClick = onLogout,
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        Spacer(Modifier.height(12.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    onMenuClick: (() -> Unit)?,
    onGoToCapsules: () -> Unit,
    onGoToCreate: () -> Unit,
    onGoToJoin: () -> Unit,
    onGoToProfile: () -> Unit,
    onGoToSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menú") },
                navigationIcon = {
                    if (onMenuClick != null) {
                        IconButton(onClick = onMenuClick) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(modifier = Modifier.fillMaxWidth(), onClick = onGoToCapsules) {
                Text("Mis cápsulas")
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = onGoToCreate) {
                Text("Crear cápsula")
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = onGoToJoin) {
                Text("Unirse a cápsula")
            }
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onGoToProfile) {
                Text("Perfil")
            }
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onGoToSettings) {
                Text("Ajustes")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userId: String?,
    onMenuClick: (() -> Unit)?,
    capsuleViewModel: CapsuleViewModel,
    onCapsuleClick: (Capsule) -> Unit,
    onNavigateToCreate: () -> Unit,
    onJoinCapsule: () -> Unit
) {
    val capsules by capsuleViewModel.capsules.collectAsState()
    val isLoading by capsuleViewModel.isLoading.collectAsState()
    val error by capsuleViewModel.error.collectAsState()

    LaunchedEffect(userId) {
        if (userId != null) capsuleViewModel.loadCapsules()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Cápsulas") },
                navigationIcon = {
                    if (onMenuClick != null) {
                        IconButton(onClick = onMenuClick) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    } else {
                        IconButton(onClick = { /* no-op */ }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
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

        if (userId == null) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {

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
                        CapsuleItem(capsule = capsule) { onCapsuleClick(capsule) }
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
            .clickable(onClick = onClick)
            .alpha(if (isOpenable) 1f else 0.6f),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userEmail: String,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Email:", style = MaterialTheme.typography.labelLarge)
            Text(text = userEmail, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "Aquí luego podemos añadir nombre, foto, etc.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                .fillMaxSize()
        ) {
            Text("Ajustes (placeholder)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "Aquí luego podemos añadir cosas como tema, about, etc.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
