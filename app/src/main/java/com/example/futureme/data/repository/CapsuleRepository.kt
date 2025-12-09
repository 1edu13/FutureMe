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

        val capsuleData = hashMapOf(
            "creatorId" to userId,
            "ownerId" to userId,
            "title" to title,
            "createdAt" to Timestamp.now(),
            "openDate" to Timestamp(openDateTime.time),
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

    suspend fun joinCapsule(capsuleId: String, userId: String) {
        dataSource.joinCapsule(capsuleId, userId)
    }


}
