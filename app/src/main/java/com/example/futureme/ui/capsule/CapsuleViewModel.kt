package com.example.futureme.ui.capsule

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futureme.data.model.Capsule
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

class CapsuleViewModel : ViewModel() {

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

    private fun loadCapsules() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("capsules")
            .whereEqualTo("creatorId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = "Error al cargar las cápsulas: ${e.message}"
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    _capsules.value = snapshot.toObjects<Capsule>()
                }
            }
    }

    fun loadCapsuleById(capsuleId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val doc = db.collection("capsules").document(capsuleId).get().await()
                val capsule = doc.toObject<Capsule>()
                _selectedCapsule.value = capsule

                if (capsule?.status == "PENDING") {
                    markCapsuleAsOpened(capsuleId)
                }

            } catch (e: Exception) {
                _error.value = "Error al cargar la cápsula: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun markCapsuleAsOpened(capsuleId: String) {
        viewModelScope.launch {
            try {
                db.collection("capsules").document(capsuleId).update("status", "OPENED").await()
            } catch (e: Exception) {
                Log.e("CapsuleViewModel", "Error al marcar la cápsula como abierta", e)
            }
        }
    }

    fun clearSelectedCapsule() {
        _selectedCapsule.value = null
    }

    fun saveCapsule(title: String, text: String, openDateTime: LocalDateTime, imageUris: List<Uri>) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.value = "Error: Usuario no autenticado."
            return
        }
        if (title.isBlank() || text.isBlank()) {
            _error.value = "El título y el texto no pueden estar vacíos."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _saveSuccess.value = false

            try {
                val imageUrls = mutableListOf<String>()
                imageUris.forEach { uri ->
                    val imageRef = storage.reference.child("capsules/$userId/${UUID.randomUUID()}")
                    imageRef.putFile(uri).await()
                    val downloadUrl = imageRef.downloadUrl.await().toString()
                    imageUrls.add(downloadUrl)
                }

                val openTimestamp = Timestamp(openDateTime.atZone(ZoneId.systemDefault()).toEpochSecond(), 0)
                val capsuleData = hashMapOf(
                    "creatorId" to userId,
                    "title" to title,
                    "text" to text,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "openDate" to openTimestamp,
                    "imageUrls" to imageUrls, 
                    "status" to "PENDING"
                )

                db.collection("capsules").add(capsuleData).await()
                _saveSuccess.value = true

            } catch (e: Exception) {
                Log.e("CapsuleViewModel", "Error al guardar la cápsula", e)
                _error.value = "Error al guardar: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}