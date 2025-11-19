package com.example.futureme.ui.navigation

/**
 * Clase sellada para definir las rutas de navegación de forma centralizada y segura.
 */
sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Home : Screen("home_screen")
    object CreateCapsule : Screen("create_capsule_screen")

    // Ruta para el detalle de la cápsula, con un argumento para el ID
    object CapsuleDetail : Screen("capsule_detail/{capsuleId}") {
        // Función de ayuda para construir la ruta con un ID concreto
        fun createRoute(capsuleId: String) = "capsule_detail/$capsuleId"
    }
}
