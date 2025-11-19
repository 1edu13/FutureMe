package com.example.futureme.data.model

import com.google.firebase.Timestamp
import java.util.Date

data class Capsule(
    val id: String,
    val creatorId: String,
    val title: String,
    val text: String,
    val createdAt: Timestamp,
    val openDate: Timestamp,
    val status: String
) {
    fun isOpenable(): Boolean {
        return Date() >= openDate.toDate()
    }
}
