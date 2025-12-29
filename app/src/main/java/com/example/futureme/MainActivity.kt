package com.example.futureme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.futureme.ui.capsule.HomeCapsuleScreen
import com.example.futureme.ui.onboarding.OnboardingScreen
import com.example.futureme.ui.settings.SettingsScreen

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

    // ✅ Estado onboarding (Firestore)
    val onboardingCompleted by authViewModel.onboardingCompleted.collectAsState()

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

    // ✅ Cuando hay usuario, cargamos onboardingCompleted desde Firestore
    LaunchedEffect(user) {
        if (user != null) {
            authViewModel.loadOnboardingState()
        }
    }

    // ✅ Si NO ha completado onboarding -> vamos a Onboarding (solo si no estamos ya ahí)
    LaunchedEffect(user, onboardingCompleted, currentRoute) {
        if (
            user != null &&
            onboardingCompleted == false &&
            currentRoute != Screen.Onboarding.route
        ) {
            navController.navigate(Screen.Onboarding.route) {
                launchSingleTop = true
            }
        }
    }

    // ✅ Solo mostramos drawer si NO estamos en login NI onboarding y hay usuario
    val showDrawer =
        user != null &&
                currentRoute != Screen.Login.route &&
                currentRoute != Screen.Onboarding.route

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
        // Sin drawer (login / onboarding)
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

        // ✅ ONBOARDING (popups/tutorial)
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    authViewModel.completeOnboarding {
                        navController.navigate(Screen.MainMenu.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(Screen.MainMenu.route) {
            MainMenuScreen(
                onMenuClick = onOpenDrawer,
                onGoToCapsules = { navController.navigate(Screen.Home.route) },
                onGoToProfile = { navController.navigate(Screen.Profile.route) },
                onGoToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        // HOME (Mis cápsulas)
        composable(Screen.Home.route) {
            HomeCapsuleScreen (
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
            com.example.futureme.ui.profile.ProfileScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onGoToEditAccount = { navController.navigate(Screen.EditAccount.route) }
            )
        }

        composable(Screen.EditAccount.route) {
            com.example.futureme.ui.profile.EditAccountScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // AJUSTES (con relanzar tutorial)
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onShowTutorialAgain = {
                    authViewModel.resetOnboarding()
                    navController.navigate(Screen.Onboarding.route) {
                        launchSingleTop = true
                    }
                }
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

@Composable
private fun AppDrawerContent(
    userEmail: String,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val gold = Color(0xFFD4AF37)
    val textPrimary = Color(0xFFEDEDED)
    val drawerBg = Color(0xFF0B0B0B)
    val drawerItemBg = Color(0xFF121212)

    ModalDrawerSheet(
        drawerContainerColor = drawerBg,
        drawerContentColor = textPrimary
    ) {
        Spacer(Modifier.height(12.dp))

        Text(
            text = "FutureMe",
            style = MaterialTheme.typography.titleLarge,
            color = textPrimary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Text(
            text = userEmail,
            style = MaterialTheme.typography.bodySmall,
            color = textPrimary.copy(alpha = 0.75f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(12.dp))
        Divider(color = gold.copy(alpha = 0.20f))

        @Composable
        fun itemColors(selected: Boolean) = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = gold.copy(alpha = 0.18f),
            unselectedContainerColor = Color.Transparent,
            selectedTextColor = textPrimary,
            unselectedTextColor = textPrimary,
            selectedIconColor = gold,
            unselectedIconColor = gold
        )

        NavigationDrawerItem(
            label = { Text("Menú") },
            selected = currentRoute == Screen.MainMenu.route,
            onClick = { onNavigate(Screen.MainMenu.route) },
            icon = { Icon(Icons.Default.Menu, contentDescription = null) },
            colors = itemColors(currentRoute == Screen.MainMenu.route),
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = { Text("Mis cápsulas") },
            selected = currentRoute == Screen.Home.route,
            onClick = { onNavigate(Screen.Home.route) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            colors = itemColors(currentRoute == Screen.Home.route),
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = { Text("Crear cápsula") },
            selected = currentRoute == Screen.CreateCapsule.route,
            onClick = { onNavigate(Screen.CreateCapsule.route) },
            icon = { Icon(Icons.Default.AddCircle, contentDescription = null) },
            colors = itemColors(currentRoute == Screen.CreateCapsule.route),
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = { Text("Unirse a cápsula") },
            selected = currentRoute == Screen.JoinCapsule.route,
            onClick = { onNavigate(Screen.JoinCapsule.route) },
            icon = { Icon(Icons.Default.Link, contentDescription = null) },
            colors = itemColors(currentRoute == Screen.JoinCapsule.route),
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        Spacer(Modifier.height(8.dp))
        Divider(color = gold.copy(alpha = 0.20f))
        Spacer(Modifier.height(8.dp))

        NavigationDrawerItem(
            label = { Text("Perfil") },
            selected = currentRoute == Screen.Profile.route,
            onClick = { onNavigate(Screen.Profile.route) },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            colors = itemColors(currentRoute == Screen.Profile.route),
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = { Text("Ajustes") },
            selected = currentRoute == Screen.Settings.route,
            onClick = { onNavigate(Screen.Settings.route) },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            colors = itemColors(currentRoute == Screen.Settings.route),
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        Spacer(Modifier.height(8.dp))
        Divider(color = gold.copy(alpha = 0.20f))
        Spacer(Modifier.height(8.dp))

        NavigationDrawerItem(
            label = { Text("Cerrar sesión") },
            selected = false,
            onClick = onLogout,
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = Color.Transparent,
                unselectedTextColor = textPrimary,
                unselectedIconColor = gold
            ),
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
    onGoToProfile: () -> Unit,
    onGoToSettings: () -> Unit
) {
    val gold = Color(0xFFD4AF37)
    val titleGold = Color(0xFFC9A84D)      // 👈 dorado suave para el título
    val textPrimary = Color(0xFFEDEDED)    // 👈 blanco roto
    val bg = Color.Black
    val cardBg = Color(0xFF0E0E0E)
    val toolbarBg = Color(0xFF161616)      // 👈 negro antracita para la toolbar

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "Menú",
                            color = titleGold,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        if (onMenuClick != null) {
                            IconButton(onClick = onMenuClick) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = "Menú",
                                    tint = gold
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = toolbarBg,
                        titleContentColor = titleGold,
                        navigationIconContentColor = gold,
                        actionIconContentColor = gold
                    )
                )

                // Línea inferior sutil (premium)
                Divider(
                    color = gold.copy(alpha = 0.20f),
                    thickness = 1.dp
                )
            }
        },
        containerColor = bg
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Fondo con la MISMA imagen del login
            Image(
                painter = painterResource(id = R.drawable.login_bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            // Overlay oscuro
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.65f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.Center
            ) {

                // -------- BOTÓN PRINCIPAL --------
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(82.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, gold.copy(alpha = 0.35f)),
                    shadowElevation = 10.dp,
                    onClick = onGoToCapsules
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cápsulas",
                            color = textPrimary,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                // -------- BOTONES SECUNDARIOS --------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(26.dp)
                ) {
                    GoldImageCardButton(
                        label = "Perfil",
                        drawableRes = R.drawable.ic_profile_gold,
                        onClick = onGoToProfile,
                        modifier = Modifier.weight(1f),
                        gold = gold,
                        textPrimary = textPrimary,
                        cardBg = cardBg
                    )
                    GoldImageCardButton(
                        label = "Ajustes",
                        drawableRes = R.drawable.ic_settings_gold,
                        onClick = onGoToSettings,
                        modifier = Modifier.weight(1f),
                        gold = gold,
                        textPrimary = textPrimary,
                        cardBg = cardBg
                    )
                }
            }
        }
    }
}


@Composable
private fun GoldImageCardButton(
    label: String,
    drawableRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gold: Color,
    textPrimary: Color,   // 👈 nuevo
    cardBg: Color
) {
    Surface(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        border = BorderStroke(0.5.dp, gold.copy(alpha = 0.25f)),
        shadowElevation = 8.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = label,
                modifier = Modifier.size(72.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = label,
                color = textPrimary,          // 👈 blanco roto
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}







