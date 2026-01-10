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
    // Instanciamos AuthRepository (ahora con getUserName)
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

    private val _contributorNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val contributorNames: StateFlow<Map<String, String>> = _contributorNames

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
                val cap = capsuleRepository.getCapsuleById(capsuleId)
                _selectedCapsule.value = cap

                if (cap == null) {
                    _error.value = "La cápsula no existe."
                } else {
                    val ids = buildSet {
                        addAll(cap.contributions.keys)
                        add(cap.creatorId)
                        add(cap.ownerId)
                    }
                    val namesMap = mutableMapOf<String, String>()

                    ids.forEach { uid ->
                        val name = authRepository.getUserName(uid)
                        if (!name.isNullOrBlank()) {
                            namesMap[uid] = name
                        }
                    }
                    _contributorNames.value = namesMap
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
        isShared: Boolean,
        openDateTime: Calendar,
        editDeadline: Calendar,
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

                capsuleRepository.saveCapsule(
                    userId = userId,
                    title = title,
                    text = text,
                    isShared = isShared,
                    openDateTime = openDateTime,
                    editDeadline = editDeadline,
                    imageUrls = imageUrls
                )

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
                val capsule = _selectedCapsule.value?.takeIf { it.id == capsuleId }
                    ?: capsuleRepository.getCapsuleById(capsuleId)

                if (capsule == null) {
                    _error.value = "La cápsula no existe."
                    return@launch
                }

                val oldContribution = capsule.contributions[userId]
                val oldText = oldContribution?.get("text") as? String ?: ""
                val oldImages = oldContribution?.get("images") as? List<String> ?: emptyList()

                val newImageUrls = storageRepository.uploadImages(context, userId, imageUris)
                val finalImages = oldImages + newImageUrls

                val finalText = when {
                    text.isBlank() -> oldText
                    oldText.isBlank() -> text
                    else -> oldText + "\n\n" + text
                }

                capsuleRepository.updateContribution(
                    capsuleId = capsuleId,
                    userId = userId,
                    text = finalText,
                    imageUrls = finalImages
                )

                loadCapsuleById(capsuleId)
                _saveSuccess.value = true

            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSelectedCapsule() {
        _selectedCapsule.value = null
        // 🔹 Limpiamos también los nombres
        _contributorNames.value = emptyMap()
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
                capsuleRepository.joinCapsule(code.trim(), userId)
                loadCapsules()
                _saveSuccess.value = true
            } catch (e: Exception) {
                Log.e("CapsuleViewModel", "Error joining capsule", e)
                _error.value = "Error al unirse: ${e.localizedMessage ?: "Error desconocido"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCapsule(capsuleId: String) {
        val userId = authRepository.getCurrentUser()?.uid
        if (userId == null) {
            _error.value = "Usuario no autenticado."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _saveSuccess.value = false

            try {
                val capsule = _selectedCapsule.value?.takeIf { it.id == capsuleId }
                    ?: capsuleRepository.getCapsuleById(capsuleId)

                if (capsule == null) {
                    _error.value = "La cápsula no existe."
                    return@launch
                }

                if (capsule.creatorId != userId) {
                    _error.value = "No tienes permisos para borrar esta cápsula."
                    return@launch
                }

                val urls = buildList {
                    addAll(capsule.images)
                    capsule.contributions.values.forEach { data ->
                        val imgs = data["images"] as? List<String> ?: emptyList()
                        addAll(imgs)
                    }
                }

                storageRepository.deleteImagesByUrls(urls)
                capsuleRepository.deleteCapsule(capsuleId)

                clearSelectedCapsule()
                loadCapsules()

                _saveSuccess.value = true
            } catch (e: Exception) {
                _error.value = "Error al eliminar: ${e.localizedMessage ?: e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun leaveCapsule(capsuleId: String) {
        val userId = authRepository.getCurrentUser()?.uid
        if (userId == null) {
            _error.value = "Usuario no autenticado."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                capsuleRepository.leaveCapsule(capsuleId, userId)
                clearSelectedCapsule()
                loadCapsules()
                _saveSuccess.value = true
            } catch (e: Exception) {
                _error.value = "Error al salir: ${e.localizedMessage ?: e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSaveSuccess() {
        _saveSuccess.value = false
    }
}