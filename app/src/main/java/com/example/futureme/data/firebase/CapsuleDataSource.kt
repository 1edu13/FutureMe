package com.example.futureme.data.firebase

import com.example.futureme.data.model.Capsule
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CapsuleDataSource(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getCapsulesByUser(userId: String): List<Capsule> {
        val result = db.collection("capsules")
            .whereArrayContains("participantIds", userId)
            .get()
            .await()

        return result.documents.mapNotNull { doc ->
            try {
                Capsule(
                    id = doc.id,
                    creatorId = doc.getString("creatorId") ?: "",
                    title = doc.getString("title") ?: "",
                    text = doc.getString("text") ?: "",
                    createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                    openDate = doc.getTimestamp("openDate") ?: Timestamp.now(),
                    status = doc.getString("status") ?: "",
                    images = doc.get("images") as? List<String> ?: emptyList()
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getCapsuleById(capsuleId: String): Capsule? {
        val doc = db.collection("capsules").document(capsuleId).get().await()

        return if (doc.exists()) {
            Capsule(
                id = doc.id,
                creatorId = doc.getString("creatorId") ?: "",
                title = doc.getString("title") ?: "",
                text = doc.getString("text") ?: "",
                createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                openDate = doc.getTimestamp("openDate") ?: Timestamp.now(),
                status = doc.getString("status") ?: "",
                images = doc.get("images") as? List<String> ?: emptyList()
            )
        } else {
            null
        }
    }

    suspend fun saveCapsule(capsuleData: Map<String, Any>) {
        db.collection("capsules").add(capsuleData).await()
    }
}
