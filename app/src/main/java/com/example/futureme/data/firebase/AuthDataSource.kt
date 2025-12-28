package com.example.futureme.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

class AuthDataSource(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun signUp(name: String, email: String, password: String) {
        val result = auth.createUserWithEmailAndPassword(email, password).await()

        val user = result.user ?: throw Exception("No se pudo crear el usuario")
        val uid = user.uid

        // 🔹 Guardamos perfil en Firestore
        val userProfile = hashMapOf(
            "name" to name,
            "email" to email,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("users").document(uid).set(userProfile).await()

        // 🔹 Guardamos displayName en Firebase Auth
        val request = userProfileChangeRequest {
            displayName = name
        }
        user.updateProfile(request).await()
    }

    fun signOut() {
        auth.signOut()
    }

    fun currentUser(): FirebaseUser? = auth.currentUser

    fun addAuthStateListener(listener: (FirebaseAuth) -> Unit) {
        auth.addAuthStateListener(listener)
    }

    // =========================
    // PERFIL / CUENTA
    // =========================

    suspend fun updateDisplayName(user: FirebaseUser, newName: String) {
        // 🔹 Firebase Auth
        val request = userProfileChangeRequest {
            displayName = newName
        }
        user.updateProfile(request).await()

        // 🔹 Firestore (mantener coherencia)
        db.collection("users")
            .document(user.uid)
            .update("name", newName)
            .await()
    }

    suspend fun deleteUserProfile(uid: String) {
        db.collection("users").document(uid).delete().await()
    }
}



