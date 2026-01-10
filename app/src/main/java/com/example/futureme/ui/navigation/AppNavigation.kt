package com.example.futureme.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Onboarding : Screen(route = "onboarding_screen")
    object MainMenu : Screen("main_menu_screen") // ✅ NUEVA
    object Home : Screen("home_screen")
    object CreateCapsule : Screen("create_capsule_screen")
    object JoinCapsule : Screen("join_capsule_screen")
    object Profile : Screen("profile_screen")
    object Settings : Screen("settings_screen")

    object EditAccount : Screen("edit_account")

    object CapsuleDetail : Screen("capsule_detail_screen/{capsuleId}") {
        fun createRoute(capsuleId: String) = "capsule_detail_screen/$capsuleId"
    }
}
