package com.example.futureme.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futureme.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _user = MutableStateFlow<FirebaseUser?>(repository.getCurrentUser())
    val user: StateFlow<FirebaseUser?> = _user

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _success = MutableStateFlow<String?>(null)

    private val _onboardingCompleted = MutableStateFlow(true)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted

    val success: StateFlow<String?> = _success

    init {
        repository.addAuthListener { auth ->
            _user.value = auth.currentUser
        }
    }

    fun clearMessages() {
        _error.value = null
        _success.value = null
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _success.value = null

            try {
                repository.signIn(email, password)
                _success.value = "Sesión iniciada"
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _success.value = null

            try {
                repository.signUp(name, email, password)
                _success.value = "Cuenta creada"
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        repository.signOut()
        _error.value = null
        _success.value = null
    }


    fun updateDisplayName(newName: String, currentPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _success.value = null

            try {
                if (newName.isBlank()) throw IllegalArgumentException("El nombre no puede estar vacío")
                if (currentPassword.isBlank()) throw IllegalArgumentException("Introduce tu contraseña actual")

                repository.reauthenticate(currentPassword)
                repository.updateDisplayName(newName)

                _success.value = "Nombre actualizado"
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error al actualizar nombre"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _success.value = null

            try {
                if (currentPassword.isBlank()) throw IllegalArgumentException("Introduce tu contraseña actual")
                if (newPassword.length < 6) throw IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres")

                repository.reauthenticate(currentPassword)
                repository.updatePassword(newPassword)

                _success.value = "Contraseña actualizada"
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error al actualizar contraseña"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAccount(currentPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _success.value = null

            try {
                if (currentPassword.isBlank()) throw IllegalArgumentException("Introduce tu contraseña actual")

                repository.reauthenticate(currentPassword)
                repository.deleteAccount()

                _success.value = "Cuenta eliminada"
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error al eliminar la cuenta"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadOnboardingState() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _onboardingCompleted.value = repository.isOnboardingCompleted()
            } catch (e: Exception) {
                _onboardingCompleted.value = true
                _error.value = e.localizedMessage ?: "Error cargando onboarding"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun completeOnboarding(onDone: () -> Unit) {
        viewModelScope.launch {
            _onboardingCompleted.value = true
            try {
                repository.setOnboardingCompleted(true) // guardado real
            } catch (_: Exception) {
                // no crashear
            } finally {
                onDone()
            }
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            _error.value = null
            try {
                repository.setOnboardingCompleted(false)
                _onboardingCompleted.value = false
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error reseteando onboarding"
            }
        }
    }


}
