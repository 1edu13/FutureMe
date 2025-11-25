package com.example.futureme.ui.capsule

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futureme.data.model.Capsule
import com.example.futureme.data.repository.CapsuleRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.UUID

class CapsuleViewModel : ViewModel() {

    private val capsuleRepository = CapsuleRepository()

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage

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
        val userId = auth.currentUser?.uid
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
                _error.value = "Error al cargar la cápsula."
            } finally {
                _isLoading.value = false
            }
        }
    }


    private suspend fun uploadImages(context: Context, uris: List<Uri>): List<String> {
        val urls = mutableListOf<String>()
        val resolver = context.contentResolver
        val userId = auth.currentUser?.uid ?: "unknown"

        for (uri in uris) {
            try {
                Log.d("Upload", "Procesando URI: $uri")

                val inputStream = resolver.openInputStream(uri)
                    ?: throw Exception("No se pudo abrir InputStream para $uri")

                val bytes = inputStream.readBytes()
                inputStream.close()

                // Estructura: capsules/USER_ID/RANDOM_ID.jpg
                val fileName = "capsules/$userId/${UUID.randomUUID()}.jpg"
                val ref = storage.reference.child(fileName)

                ref.putBytes(bytes).await()
                val downloadUrl = ref.downloadUrl.await().toString()
                urls.add(downloadUrl)

                Log.d("Upload", "Imagen subida correctamente: $downloadUrl")

            } catch (e: Exception) {
                Log.e("Upload", "Error al subir imagen: ${e.localizedMessage}")
                throw e // Re-anzamos para que saveCapsule lo capture
            }
        }

        return urls
    }

    fun saveCapsule(
        title: String,
        text: String,
        openDateTime: Calendar,
        imageUris: List<Uri>,
        context: Context
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.value = "Error: Usuario no autenticado."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _saveSuccess.value = false

            try {
                // 1. Subir imágenes a Storage y obtener URLs
                val imageUrls = uploadImages(context, imageUris)

                // 2. Preparar datos de la cápsula
                val capsuleData = hashMapOf(
                    "creatorId" to userId,
                    "ownerId" to userId,
                    "title" to title,
                    "text" to text,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "openDate" to Timestamp(openDateTime.time),
                    "status" to "scheduled",
                    "images" to imageUrls, // Guardamos las URLs, no el Base64
                    "participantIds" to listOf(userId)
                )

                // 3. Guardar en Firestore
                db.collection("capsules").add(capsuleData).await()
                
                loadCapsules()
                _saveSuccess.value = true

            } catch (e: Exception) {
                Log.e("CapsuleViewModel", "Error al guardar cápsula", e)
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSelectedCapsule() {
        _selectedCapsule.value = null
    }
}
