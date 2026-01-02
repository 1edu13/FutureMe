package com.example.futureme.data.repository

import com.example.futureme.data.firebase.CapsuleDataSource
import com.example.futureme.data.model.Capsule
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.UUID

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
        isShared: Boolean,
        editDeadline: Calendar,
        openDateTime: Calendar,
        imageUrls: List<String>
    ) {

        val capsuleId = java.util.UUID.randomUUID().toString()
        val deadlineCal = (editDeadline.clone() as Calendar)
        val openCal = (openDateTime.clone() as Calendar)
        if (openCal.before(deadlineCal)) {
            openCal.timeInMillis = deadlineCal.timeInMillis
            // opcional: openCal.add(Calendar.MINUTE, 1)
        }
        val inviteCode = UUID.randomUUID()
            .toString()
            .replace("-", "")
            .take(6)
            .uppercase()


        val capsuleData = hashMapOf(
            "creatorId" to userId,
            "ownerId" to userId,
            "title" to title,
            "isShared" to isShared,
            "inviteCode" to inviteCode,
            "createdAt" to Timestamp.now(),
            "editDeadline" to Timestamp(deadlineCal.time),
            "openDate" to Timestamp(openCal.time),
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

    suspend fun joinCapsule(inviteCode: String, userId: String): Capsule {
        return dataSource.joinCapsule(inviteCode, userId)
    }

    suspend fun deleteCapsule(capsuleId: String) {
        dataSource.deleteCapsule(capsuleId)
    }

    suspend fun leaveCapsule(capsuleId: String, userId: String) {
        dataSource.leaveCapsule(capsuleId, userId)
    }


}
