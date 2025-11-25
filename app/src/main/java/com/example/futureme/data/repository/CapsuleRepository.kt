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

        val capsuleData = hashMapOf(
            "creatorId" to userId,
            "ownerId" to userId,
            "title" to title,
            "text" to text,
            "createdAt" to Timestamp.now(),
            "openDate" to Timestamp(openDateTime.time),
            "status" to "scheduled",
            "images" to imageUrls,
            "participantIds" to listOf(userId)
        )

        dataSource.saveCapsule(capsuleData)
    }
}
