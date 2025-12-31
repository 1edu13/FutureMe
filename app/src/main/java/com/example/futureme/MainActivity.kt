package com.example.futureme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.futureme.ui.auth.AuthViewModel
import com.example.futureme.ui.auth.LoginScreen
import com.example.futureme.ui.capsule.CapsuleDetailScreen
import com.example.futureme.ui.capsule.CapsuleViewModel
import com.example.futureme.ui.capsule.CreateCapsuleScreen
import com.example.futureme.ui.capsule.HomeCapsuleScreen
import com.example.futureme.ui.capsule.JoinCapsuleScreen
import com.example.futureme.ui.menu.MainMenuScreen
import com.example.futureme.ui.navigation.Screen
import com.example.futureme.ui.onboarding.OnboardingScreen
import com.example.futureme.ui.settings.SettingsScreen
import com.example.futureme.ui.theme.FutureMeTheme
import com.example.futureme.ui.theme.ThemeViewModel
import com.example.futureme.ui.theme.ThemeViewModelFactory
import com.example.futureme.ui.menu.AppDrawerContent

import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val capsuleViewModel: CapsuleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val themeVm: ThemeViewModel = viewModel(factory = ThemeViewModelFactory(context))
            val isDark by themeVm.isDark.collectAsState()

            FutureMeTheme(darkTheme = isDark) {
                AppNavHost(
                    authViewModel = authViewModel,
                    capsuleViewModel = capsuleViewModel,
                    isDark = isDark,
                    onToggleTheme = { enabled -> themeVm.setDark(enabled) }
                )
            }
        }
    }
}

@Composable
fun AppNavHost(
    authViewModel: AuthViewModel,
    capsuleViewModel: CapsuleViewModel,
    isDark: Boolean,
    onToggleTheme: (Boolean) -> Unit
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
        if (user != null) authViewModel.loadOnboardingState()
    }

    // ✅ Si NO ha completado onboarding -> vamos a Onboarding (solo si no estamos ya ahí)
    LaunchedEffect(user, onboardingCompleted, currentRoute) {
        if (
            user != null &&
            onboardingCompleted == false &&
            currentRoute != Screen.Onboarding.route
        ) {
            navController.navigate(Screen.Onboarding.route) { launchSingleTop = true }
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
                        navController.navigate(route) { launchSingleTop = true }
                    },
                    onLogout = {
                        scope.launch { drawerState.close() }
                        authViewModel.signOut()
                    },
                    isDark = isDark
                )
            }
        ) {
            AppNavContent(
                navController = navController,
                authViewModel = authViewModel,
                capsuleViewModel = capsuleViewModel,
                userId = user?.uid,
                onOpenDrawer = { scope.launch { drawerState.open() } },
                isDark = isDark,
                onToggleTheme = onToggleTheme
            )
        }
    } else {
        // Sin drawer (login / onboarding)
        AppNavContent(
            navController = navController,
            authViewModel = authViewModel,
            capsuleViewModel = capsuleViewModel,
            userId = user?.uid,
            onOpenDrawer = null,
            isDark = isDark,
            onToggleTheme = onToggleTheme
        )
    }
}

@Composable
private fun AppNavContent(
    navController: androidx.navigation.NavHostController,
    authViewModel: AuthViewModel,
    capsuleViewModel: CapsuleViewModel,
    userId: String?,
    onOpenDrawer: (() -> Unit)?,
    isDark: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {

        // LOGIN
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                isDark = isDark
            )
        }

        // ✅ ONBOARDING (popups/tutorial)
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                isDark = isDark,
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

        // MENÚ
        composable(Screen.MainMenu.route) {
            MainMenuScreen(
                isDark = isDark,
                onMenuClick = onOpenDrawer,
                onGoToCapsules = { navController.navigate(Screen.Home.route) },
                onGoToProfile = { navController.navigate(Screen.Profile.route) },
                onGoToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        // HOME (Mis cápsulas)
        composable(Screen.Home.route) {
            HomeCapsuleScreen(
                isDark = isDark,
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
                isDark = isDark,
                capsuleViewModel = capsuleViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // UNIRSE A CÁPSULA
        composable(Screen.JoinCapsule.route) {
            JoinCapsuleScreen(
                isDark = isDark,
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

        // AJUSTES
        composable(Screen.Settings.route) {
            SettingsScreen(
                isDark = isDark,
                onToggleTheme = onToggleTheme,
                onNavigateBack = { navController.popBackStack() },
                onShowTutorialAgain = {
                    authViewModel.resetOnboarding()
                    navController.navigate(Screen.Onboarding.route) { launchSingleTop = true }
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
                onNavigateBack = { navController.popBackStack() },
                isDark = isDark,
            )
        }
    }
}

