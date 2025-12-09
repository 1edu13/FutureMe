package com.example.futureme.ui.capsule

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futureme.data.model.Capsule
import com.example.futureme.data.repository.AuthRepository
import com.example.futureme.data.repository.CapsuleRepository
import com.example.futureme.data.repository.StorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class CapsuleViewModel : ViewModel() {

    private val capsuleRepository = CapsuleRepository()
    private val storageRepository = StorageRepository()
    private val authRepository = AuthRepository()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _capsules = MutableStateFlow<List<Capsule>>(emptyList())
    val capsules: StateFlow<List<Capsule>> = _capsules

    private val _selectedCapsule = MutableStateFlow<Capsule?>(null)
    val selectedCapsule: StateFlow<Capsule?> = _selectedCapsule

    init {
        loadCapsules()
    }

    fun loadCapsules() {
        val userId = authRepository.getCurrentUser()?.uid
        if (userId == null) {
            _error.value = "Usuario no autenticado."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val capsuleList = capsuleRepository.getCapsules(userId)
                _capsules.value = capsuleList
            } catch (e: Exception) {
                Log.e("CapsuleViewModel", "Error loading capsules", e)
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun loadCapsuleById(capsuleId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _selectedCapsule.value = capsuleRepository.getCapsuleById(capsuleId)
                    ?: run {
                        _error.value = "La cápsula no existe."
                        null
                    }
            } catch (e: Exception) {
                Log.e("CapsuleViewModel", "Error loading capsule details", e)
                _error.value = "Error al cargar la cápsula: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveCapsule(
        title: String,
        text: String,
        openDateTime: Calendar,
        imageUris: List<Uri>,
        context: Context
    ) {
        val userId = authRepository.getCurrentUser()?.uid
        if (userId == null) {
            _error.value = "Error: Usuario no autenticado."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _saveSuccess.value = false

            try {
                // 1. Subir imágenes a Storage (repo)
                val imageUrls = storageRepository.uploadImages(context, userId, imageUris)

                // 2. Guardar cápsula (repo)
                capsuleRepository.saveCapsule(
                    userId = userId,
                    title = title,
                    text = text,
                    openDateTime = openDateTime,
                    imageUrls = imageUrls
                )

                // 3. Volver a cargar la lista
                loadCapsules()

                _saveSuccess.value = true

            } catch (e: Exception) {
                Log.e("CapsuleViewModel", "Error saving capsule", e)
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateContribution(
        capsuleId: String,
        text: String,
        imageUris: List<Uri>,
        context: Context
    ) {
        val userId = authRepository.getCurrentUser()?.uid
        if (userId == null) {
            _error.value = "Error: Usuario no autenticado."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _saveSuccess.value = false

            try {
                val imageUrls = storageRepository.uploadImages(context, userId, imageUris)

                capsuleRepository.updateContribution(
                    capsuleId = capsuleId,
                    userId = userId,
                    text = text,
                    imageUrls = imageUrls
                )

                // Recargar detalles
                loadCapsuleById(capsuleId)
                _saveSuccess.value = true

            } catch (e: Exception) {
                Log.e("CapsuleViewModel", "Error updating contribution", e)
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun clearSelectedCapsule() {
        _selectedCapsule.value = null
    }

    fun joinCapsule(code: String) {
        val userId = authRepository.getCurrentUser()?.uid
        if (userId == null) {
            _error.value = "Usuario no autenticado."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Limpiamos espacios en blanco por si acaso al copiar/pegar
                capsuleRepository.joinCapsule(code.trim(), userId)

                // Volvemos a cargar las cápsulas del usuario
                loadCapsules()

                _saveSuccess.value = true
            } catch (e: Exception) {
                Log.e("CapsuleViewModel", "Error joining capsule", e)
                // Mostramos el error real para facilitar el debug
                _error.value = "Error al unirse: ${e.localizedMessage ?: "Error desconocido"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

}
