package com.example.futureme.data.firebase

import com.example.futureme.data.model.Capsule
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FieldValue

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

    suspend fun saveCapsule(capsuleId: String, capsuleData: Map<String, Any>) {
        db.collection("capsules")
            .document(capsuleId)
            .set(capsuleData)
            .await()
    }


    suspend fun joinCapsule(capsuleId: String, userId: String) {
        // Contribution vacía para el nuevo participante
        val emptyContribution = mapOf(
            "text" to "",
            "images" to emptyList<String>()
        )

        // Actualizamos participantIds y creamos su contribution
        db.collection("capsules")
            .document(capsuleId)
            .update(
                mapOf(
                    "participantIds" to FieldValue.arrayUnion(userId),
                    "contributions.$userId" to emptyContribution
                )
            )
            .await()
    }
}
