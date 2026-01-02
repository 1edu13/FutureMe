package com.example.futureme

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
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
import com.example.futureme.ui.menu.AppDrawerContent
import com.example.futureme.ui.menu.MainMenuScreen
import com.example.futureme.ui.navigation.Screen
import com.example.futureme.ui.onboarding.OnboardingScreen
import com.example.futureme.ui.settings.SettingsScreen
import com.example.futureme.ui.theme.FutureMeTheme
import com.example.futureme.ui.theme.ThemeViewModel
import com.example.futureme.ui.theme.ThemeViewModelFactory
import kotlinx.coroutines.launch
import java.util.Locale

// ✅ CAMBIO 1: Heredar de AppCompatActivity para el idioma
class MainActivity : AppCompatActivity() {

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

            // ✅ CAMBIO 2: Detectar idioma actual
            val localeList = AppCompatDelegate.getApplicationLocales()
            val currentLanguageCode = if (!localeList.isEmpty) {
                localeList[0]?.language ?: Locale.getDefault().language
            } else {
                Locale.getDefault().language
            }

            FutureMeTheme(darkTheme = isDark) {
                AppNavHost(
                    authViewModel = authViewModel,
                    capsuleViewModel = capsuleViewModel,
                    isDark = isDark,
                    currentLanguageCode = currentLanguageCode,
                    onToggleTheme = { enabled -> themeVm.setDark(enabled) },
                    onLanguageChange = { newCode ->
                        // ✅ CAMBIO 3: Cambiar idioma nativamente
                        val appLocale = LocaleListCompat.forLanguageTags(newCode)
                        AppCompatDelegate.setApplicationLocales(appLocale)
                    }
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
    currentLanguageCode: String,
    onToggleTheme: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit
) {
    val navController = rememberNavController()
    val user by authViewModel.user.collectAsState()
    val onboardingCompleted by authViewModel.onboardingCompleted.collectAsState()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(user) {
        val target = if (user == null) Screen.Login.route else Screen.MainMenu.route
        navController.navigate(target) {
            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
            launchSingleTop = true
        }
    }

    LaunchedEffect(user) {
        if (user != null) authViewModel.loadOnboardingState()
    }

    LaunchedEffect(user, onboardingCompleted, currentRoute) {
        if (user != null && onboardingCompleted == false && currentRoute != Screen.Onboarding.route) {
            navController.navigate(Screen.Onboarding.route) { launchSingleTop = true }
        }
    }

    val showDrawer = user != null && currentRoute != Screen.Login.route && currentRoute != Screen.Onboarding.route

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
                currentLanguageCode = currentLanguageCode,
                onToggleTheme = onToggleTheme,
                onLanguageChange = onLanguageChange
            )
        }
    } else {
        AppNavContent(
            navController = navController,
            authViewModel = authViewModel,
            capsuleViewModel = capsuleViewModel,
            userId = user?.uid,
            onOpenDrawer = null,
            isDark = isDark,
            currentLanguageCode = currentLanguageCode,
            onToggleTheme = onToggleTheme,
            onLanguageChange = onLanguageChange
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
    currentLanguageCode: String,
    onToggleTheme: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(authViewModel = authViewModel, isDark = isDark)
        }

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

        composable(Screen.MainMenu.route) {
            MainMenuScreen(
                isDark = isDark,
                onMenuClick = onOpenDrawer,
                onGoToCapsules = { navController.navigate(Screen.Home.route) },
                onGoToProfile = { navController.navigate(Screen.Profile.route) },
                onGoToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Home.route) {
            HomeCapsuleScreen(
                isDark = isDark,
                userId = userId,
                onMenuClick = onOpenDrawer,
                capsuleViewModel = capsuleViewModel,
                onCapsuleClick = { capsule -> navController.navigate(Screen.CapsuleDetail.createRoute(capsule.id)) },
                onNavigateToCreate = { navController.navigate(Screen.CreateCapsule.route) },
                onJoinCapsule = { navController.navigate(Screen.JoinCapsule.route) }
            )
        }

        composable(Screen.CreateCapsule.route) {
            CreateCapsuleScreen(
                isDark = isDark,
                capsuleViewModel = capsuleViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.JoinCapsule.route) {
            JoinCapsuleScreen(
                isDark = isDark,
                capsuleViewModel = capsuleViewModel,
                onNavigateBack = { navController.popBackStack() },
                onJoined = { navController.navigate(Screen.Home.route) }
            )
        }

        composable(Screen.Profile.route) {
            com.example.futureme.ui.profile.ProfileScreen(
                isDark = isDark,
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onGoToEditAccount = { navController.navigate(Screen.EditAccount.route) }
            )
        }

        composable(Screen.EditAccount.route) {
            com.example.futureme.ui.profile.EditAccountScreen(
                isDark = isDark,
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ✅ CAMBIO 4: Pasar parámetros de idioma a SettingsScreen
        composable(Screen.Settings.route) {
            SettingsScreen(
                isDark = isDark,
                currentLanguageCode = currentLanguageCode,
                onToggleTheme = onToggleTheme,
                onLanguageChange = onLanguageChange,
                onNavigateBack = { navController.popBackStack() },
                onShowTutorialAgain = {
                    authViewModel.resetOnboarding()
                    navController.navigate(Screen.Onboarding.route) { launchSingleTop = true }
                }
            )
        }

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