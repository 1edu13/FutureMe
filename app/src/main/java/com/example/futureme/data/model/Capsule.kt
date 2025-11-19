package com.example.futureme.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Representa la estructura de datos de una cápsula del tiempo en Firestore.
 */
data class Capsule(
    @DocumentId val id: String = "",
    val title: String = "",
    val creatorId: String = "",
    val openDate: Timestamp? = null,
    val status: String = "",
    val text: String = "",
    val imageUrls: List<String> = emptyList(),
    val createdAt: Timestamp? = null
)
