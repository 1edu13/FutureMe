package com.example.futureme.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore

    private val _user = MutableStateFlow(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _user.value = firebaseAuth.currentUser
        }
    }

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _error.value = "El email y la contraseña no pueden estar vacíos."
            return
        }
        viewModelScope.launch {
            _error.value = null
            _isLoading.value = true
            try {
                auth.signInWithEmailAndPassword(email, password).await()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error de inicio de sesión", e)
                _error.value = e.localizedMessage ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUp(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _error.value = "Nombre, email y contraseña son obligatorios."
            return
        }
        viewModelScope.launch {
            _error.value = null
            _isLoading.value = true
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()

                authResult.user?.let { newUser ->
                    val userProfile = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                    db.collection("users").document(newUser.uid).set(userProfile).await()
                }

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error de registro", e)
                _error.value = e.localizedMessage ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _error.value = null
    }
}