package com.example.futureme.ui.capsule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futureme.data.model.Capsule
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class CapsuleViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

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
                val result = db.collection("capsules")
                    .whereEqualTo("creatorId", userId)
                    .get()
                    .await()

                val capsuleList = result.documents.mapNotNull { doc ->
                    Capsule(
                        id = doc.id,
                        creatorId = doc.getString("creatorId") ?: "",
                        title = doc.getString("title") ?: "",
                        text = doc.getString("text") ?: "",
                        createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                        openDate = doc.getTimestamp("openDate") ?: Timestamp.now(),
                        status = doc.getString("status") ?: ""
                    )
                }
                _capsules.value = capsuleList
            } catch (e: Exception) {
                _error.value = "Error al cargar las cápsulas: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadCapsuleById(capsuleId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val doc = db.collection("capsules").document(capsuleId).get().await()
                if (doc.exists()) {
                    val capsule = Capsule(
                        id = doc.id,
                        creatorId = doc.getString("creatorId") ?: "",
                        title = doc.getString("title") ?: "",
                        text = doc.getString("text") ?: "",
                        createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                        openDate = doc.getTimestamp("openDate") ?: Timestamp.now(),
                        status = doc.getString("status") ?: ""
                    )
                    // Check if the user is authorized to see it
                    if (capsule.creatorId == auth.currentUser?.uid) {
                        _selectedCapsule.value = capsule
                    } else {
                         _error.value = "No tienes permiso para ver esta cápsula."
                    }
                } else {
                    _error.value = "La cápsula no existe."
                }
            } catch (e: Exception) {
                Log.e("CapsuleViewModel", "Error loading capsule by ID", e)
                _error.value = "Error al cargar la cápsula: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearSelectedCapsule() {
        _selectedCapsule.value = null
    }

    fun saveCapsule(title: String, text: String, openDateTime: Calendar) {
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
                val openTimestamp = Timestamp(openDateTime.time)

                val capsuleData = hashMapOf(
                    "creatorId" to userId,
                    "title" to title,
                    "text" to text,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "openDate" to openTimestamp,
                    "status" to "PENDING"
                )

                db.collection("capsules").add(capsuleData).await()
                loadCapsules()
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