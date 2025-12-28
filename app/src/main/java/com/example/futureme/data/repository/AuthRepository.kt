package com.example.futureme.data.repository

import com.example.futureme.data.firebase.AuthDataSource
import com.google.firebase.auth.EmailAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val dataSource: AuthDataSource = AuthDataSource()
) {

    fun getCurrentUser() = dataSource.currentUser()

    fun addAuthListener(listener: (auth: com.google.firebase.auth.FirebaseAuth) -> Unit) {
        dataSource.addAuthStateListener(listener)
    }

    suspend fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            throw IllegalArgumentException("Email y contraseña requeridos")
        }
        dataSource.signIn(email, password)
    }

    suspend fun signUp(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            throw IllegalArgumentException("Todos los campos son obligatorios")
        }
        dataSource.signUp(name, email, password)
    }

    fun signOut() {
        dataSource.signOut()
    }

    // =========================
    // 🔐 PERFIL / CUENTA
    // =========================

    /**
     * Firebase exige reautenticación para acciones sensibles
     */
    suspend fun reauthenticate(currentPassword: String) {
        val user = dataSource.currentUser()
            ?: throw IllegalStateException("No hay usuario autenticado")

        val email = user.email
            ?: throw IllegalStateException("El usuario no tiene email")

        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential).await()
    }

    /**
     * Cambiar nombre visible (displayName)
     */
    suspend fun updateDisplayName(newName: String) {
        val user = dataSource.currentUser()
            ?: throw IllegalStateException("No hay usuario autenticado")

        dataSource.updateDisplayName(user, newName)
    }

    /**
     * Cambiar contraseña (requiere reauth antes)
     */
    suspend fun updatePassword(newPassword: String) {
        val user = dataSource.currentUser()
            ?: throw IllegalStateException("No hay usuario autenticado")

        user.updatePassword(newPassword).await()
    }

    /**
     * Eliminar cuenta definitivamente (requiere reauth antes)
     */
    suspend fun deleteAccount() {
        val user = dataSource.currentUser()
            ?: throw IllegalStateException("No hay usuario autenticado")

        // 1) Borra perfil en Firestore
        dataSource.deleteUserProfile(user.uid)

        // 2) Borra usuario en Firebase Auth
        user.delete().await()

        // 3) Limpieza
        dataSource.signOut()
    }

}
