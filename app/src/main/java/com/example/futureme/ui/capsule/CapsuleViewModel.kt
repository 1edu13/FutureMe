package com.example.futureme.ui.capsule

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.ZoneId

class CapsuleViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

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
                // Convertimos el LocalDateTime a un Timestamp de Firebase
                val openTimestamp = Timestamp(openDateTime.atZone(ZoneId.systemDefault()).toEpochSecond(), 0)

                val capsuleData = hashMapOf(
                    "creatorId" to userId,
                    "title" to title,
                    "text" to text,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "openDate" to openTimestamp, // Guardamos la fecha de apertura
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