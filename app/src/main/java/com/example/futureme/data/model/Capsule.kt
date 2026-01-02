package com.example.futureme.data.model

import com.google.firebase.Timestamp
import java.util.Date

data class Capsule(
    val id: String = "",
    val creatorId: String = "",
    val ownerId: String = "",
    val participantIds: List<String> = emptyList(),

    val title: String = "",
    val text: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val openDate: Timestamp = Timestamp.now(),
    val editDeadline: Timestamp = Timestamp.now(),
    val isShared: Boolean = false,
    val inviteCode: String = "",
    val status: String = "",
    val images: List<String> = emptyList(),
    val contributions: Map<String, Map<String, Any>> = emptyMap()
) {
    fun isOpenable(): Boolean = Date() >= openDate.toDate()
    fun isEditable(): Boolean = Date() < editDeadline.toDate()
    fun canJoin(): Boolean = Date() < editDeadline.toDate()
}

