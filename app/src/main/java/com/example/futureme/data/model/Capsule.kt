package com.example.futureme.data.model

import com.google.firebase.Timestamp
import java.util.Date

data class Capsule(
    val id: String = "",
    val creatorId: String = "",
    val title: String = "",
    val text: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val openDate: Timestamp = Timestamp.now(),
    val status: String = "",
    val images: List<String> = emptyList() // ← AÑADIDO
) {
    fun isOpenable(): Boolean {
        return Date() >= openDate.toDate()
    }
}
