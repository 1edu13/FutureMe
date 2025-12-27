package com.example.futureme.ui.navigation

/**
 * Clase sellada para definir las rutas de navegación de forma centralizada y segura.
 * Esto evita errores al escribir strings y facilita el mantenimiento.
 */
sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object MainMenu : Screen("main_menu_screen") // ✅ NUEVA
    object Home : Screen("home_screen")
    object CreateCapsule : Screen("create_capsule_screen")
    object JoinCapsule : Screen("join_capsule_screen")
    object Profile : Screen("profile_screen")
    object Settings : Screen("settings_screen")
    object CapsuleDetail : Screen("capsule_detail_screen/{capsuleId}") {
        fun createRoute(capsuleId: String) = "capsule_detail_screen/$capsuleId"
    }
}
