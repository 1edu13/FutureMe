package com.example.futureme.data.firebase

import com.example.futureme.data.model.Capsule
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FieldValue
import java.util.Calendar

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
                mapDocumentToCapsule(doc)
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getCapsuleById(capsuleId: String): Capsule? {
        val doc = db.collection("capsules").document(capsuleId).get().await()

        return if (doc.exists()) {
            mapDocumentToCapsule(doc)
        } else {
            null
        }
    }

    private fun mapDocumentToCapsule(doc: com.google.firebase.firestore.DocumentSnapshot): Capsule {
        // Si la cápsula no tiene fecha límite (es antigua), usamos la fecha de apertura como límite
        // para que siga siendo editable. Las nuevas cápsulas sí tendrán editDeadline.
        val openDate = doc.getTimestamp("openDate") ?: Timestamp.now()
        val editDeadline = doc.getTimestamp("editDeadline") ?: openDate

        return Capsule(
            id = doc.id,
            creatorId = doc.getString("creatorId") ?: "",
            title = doc.getString("title") ?: "",
            text = doc.getString("text") ?: "",
            createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
            openDate = openDate,
            editDeadline = editDeadline,
            status = doc.getString("status") ?: "",
            images = doc.get("images") as? List<String> ?: emptyList(),
            contributions = (doc.get("contributions") as? Map<String, Map<String, Any>>) ?: emptyMap()
        )
    }

    suspend fun saveCapsule(capsuleId: String, capsuleData: Map<String, Any>) {
        db.collection("capsules")
            .document(capsuleId)
            .set(capsuleData)
            .await()
    }
    
    suspend fun updateContribution(
        capsuleId: String, 
        userId: String, 
        text: String, 
        imageUrls: List<String>
    ) {
        val contributionData = mapOf(
            "text" to text,
            "images" to imageUrls
        )
        
        db.collection("capsules")
            .document(capsuleId)
            .update("contributions.$userId", contributionData)
            .await()
    }

    suspend fun joinCapsule(capsuleId: String, userId: String) {
        val docRef = db.collection("capsules").document(capsuleId)
        val snapshot = docRef.get().await()

        if (!snapshot.exists()) {
            throw Exception("No se encontró ninguna cápsula con este código.")
        }

        // 1) Comprobar si ya está unido
        val participants = snapshot.get("participantIds") as? List<*>
        if (participants != null && participants.contains(userId)) {
            throw Exception("Ya estás unido a esta cápsula.")
        }

        // 2) Regla nueva: no se puede unir si ya pasó el editDeadline
        val editDeadline = snapshot.getTimestamp("editDeadline")
            ?: throw Exception("La cápsula no tiene editDeadline definido.")

        if (java.util.Date() >= editDeadline.toDate()) {
            throw Exception("El plazo para unirse a esta cápsula ya ha terminado.")
        }

        // 3) Contribution vacía para el nuevo participante
        val emptyContribution = mapOf(
            "text" to "",
            "images" to emptyList<String>()
        )

        // 4) Actualizamos participantIds y creamos su contribution
        // (NO tocamos openDate ni editDeadline)
        docRef.update(
            mapOf(
                "participantIds" to FieldValue.arrayUnion(userId),
                "contributions.$userId" to emptyContribution
            )
        ).await()
    }

}
