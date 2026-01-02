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
        val isShared = doc.getBoolean("isShared") ?: false
        val inviteCode = doc.getString("inviteCode") ?: ""


        return Capsule(
            id = doc.id,
            creatorId = doc.getString("creatorId") ?: "",
            ownerId = doc.getString("ownerId") ?: (doc.getString("creatorId") ?: ""), // fallback
            participantIds = doc.get("participantIds") as? List<String> ?: emptyList(),

            title = doc.getString("title") ?: "",
            text = doc.getString("text") ?: "",
            createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
            openDate = openDate,
            editDeadline = editDeadline,
            isShared = isShared,
            inviteCode = inviteCode,
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

    suspend fun joinCapsule(inviteCode: String, userId: String): Capsule {
        val db = FirebaseFirestore.getInstance()

        // 1) Buscar cápsula por inviteCode
        val querySnap = db.collection("capsules")
            .whereEqualTo("inviteCode", inviteCode.trim().uppercase())
            .limit(1)
            .get()
            .await()

        if (querySnap.isEmpty) {
            throw Exception("Código inválido")
        }

        val capsuleDoc = querySnap.documents.first()
        val capsuleId = capsuleDoc.id

        // 2) A partir de aquí, sigues como antes pero usando capsuleId REAL
        //    (añadir al array members, o contributions, o lo que estés haciendo)
        // 2) Unirse de verdad (tu esquema usa participantIds)
        db.collection("capsules")
            .document(capsuleId)
            .update("participantIds", FieldValue.arrayUnion(userId))
            .await()

        val contributionData = mapOf(
            "text" to "",
            "images" to emptyList<String>()
        )

        db.collection("capsules")
            .document(capsuleId)
            .update("contributions.$userId", contributionData)
            .await()


        // 3) Devuelve la cápsula actualizada (si tu código lo requiere)
        val updated = db.collection("capsules").document(capsuleId).get().await()
        return mapDocumentToCapsule(updated)
    }

    suspend fun deleteCapsule(capsuleId: String) {
        db.collection("capsules")
            .document(capsuleId)
            .delete()
            .await()
    }

    suspend fun leaveCapsule(capsuleId: String, userId: String) {
        val docRef = db.collection("capsules").document(capsuleId)

        db.runTransaction { tx ->
            val snap = tx.get(docRef)
            if (!snap.exists()) return@runTransaction

            val participants = (snap.get("participantIds") as? List<String>)?.toMutableList() ?: mutableListOf()
            if (!participants.contains(userId)) return@runTransaction

            val ownerId = snap.getString("ownerId") ?: (snap.getString("creatorId") ?: "")

            // Quitar al usuario
            participants.remove(userId)

            // 1) Si ya no queda nadie -> borrar documento
            if (participants.isEmpty()) {
                tx.delete(docRef)
                return@runTransaction
            }

            // 2) Si el que se va es el anfitrión -> traspasar a otro
            val updates = hashMapOf<String, Any>(
                "participantIds" to participants,
                "contributions.$userId" to FieldValue.delete() // opcional: borrar su contribución
            )

            if (ownerId == userId) {
                updates["ownerId"] = participants.first() // simple y determinista
            }

            tx.update(docRef, updates)
        }.await()
    }




}
