package com.example.futureme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.futureme.ui.auth.AuthViewModel
import com.example.futureme.ui.auth.LoginScreen
import com.example.futureme.ui.capsule.CapsuleViewModel
import com.example.futureme.ui.capsule.CreateCapsuleScreen
import com.example.futureme.ui.capsule.CapsuleDetailScreen
import com.example.futureme.ui.home.HomeScreen
import com.example.futureme.ui.navigation.Screen
import com.example.futureme.ui.theme.FUTUREMETheme

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val capsuleViewModel: CapsuleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FUTUREMETheme {
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

    LaunchedEffect(user) {
        if (user != null) {
            navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } }
        } else {
            navController.navigate(Screen.Login.route) { popUpTo(Screen.Home.route) { inclusive = true } }
        }
    }

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(authViewModel = authViewModel)
        }
        composable(Screen.Home.route) {
            HomeScreen(
                authViewModel = authViewModel,
                capsuleViewModel = capsuleViewModel,
                onNavigateToCreate = { navController.navigate(Screen.CreateCapsule.route) },
                onCapsuleClick = { capsuleId -> 
                    navController.navigate(Screen.CapsuleDetail.createRoute(capsuleId))
                }
            )
        }
        composable(Screen.CreateCapsule.route) {
            CreateCapsuleScreen(
                capsuleViewModel = capsuleViewModel, 
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.CapsuleDetail.route,
            arguments = listOf(navArgument("capsuleId") { type = NavType.StringType })
        ) {
            val capsuleId = it.arguments?.getString("capsuleId")
            CapsuleDetailScreen(
                capsuleId = capsuleId,
                viewModel = capsuleViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
