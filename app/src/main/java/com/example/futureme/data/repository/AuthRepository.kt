package com.example.futureme.data.repository

import com.example.futureme.data.firebase.AuthDataSource

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
}
