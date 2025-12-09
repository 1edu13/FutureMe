package com.example.futureme.data.repository

import com.example.futureme.data.firebase.CapsuleDataSource
import com.example.futureme.data.model.Capsule
import com.google.firebase.Timestamp
import java.util.Calendar

class CapsuleRepository(
    private val dataSource: CapsuleDataSource = CapsuleDataSource()
) {

    suspend fun getCapsules(userId: String): List<Capsule> {
        return dataSource.getCapsulesByUser(userId)
    }

    suspend fun getCapsuleById(capsuleId: String): Capsule? {
        return dataSource.getCapsuleById(capsuleId)
    }

    suspend fun saveCapsule(
        userId: String,
        title: String,
        text: String,
        openDateTime: Calendar,
        imageUrls: List<String>
    ) {

        val capsuleId = java.util.UUID.randomUUID().toString()
        
        // Calculamos la fecha límite (24h después de crearla)
        val deadlineCal = Calendar.getInstance()
        deadlineCal.add(Calendar.HOUR_OF_DAY, 24)

        val capsuleData = hashMapOf(
            "creatorId" to userId,
            "ownerId" to userId,
            "title" to title,
            "createdAt" to Timestamp.now(),
            "openDate" to Timestamp(openDateTime.time),
            "editDeadline" to Timestamp(deadlineCal.time),
            "status" to "scheduled",
            "participantIds" to listOf(userId),
            "contributions" to mapOf(
                userId to mapOf(
                    "text" to text,
                    "images" to imageUrls.ifEmpty { emptyList<String>() }
                )
            )
        )

        dataSource.saveCapsule(capsuleId, capsuleData)
    }
    
    suspend fun updateContribution(
        capsuleId: String,
        userId: String,
        text: String,
        imageUrls: List<String>
    ) {
        dataSource.updateContribution(capsuleId, userId, text, imageUrls)
    }

    suspend fun joinCapsule(capsuleId: String, userId: String) {
        dataSource.joinCapsule(capsuleId, userId)
    }
}
