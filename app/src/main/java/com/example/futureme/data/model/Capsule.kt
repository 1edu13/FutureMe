package com.example.futureme.data.model

import com.google.firebase.Timestamp

data class Capsule(
    val id: String,
    val title: String,
    val text: String,
    val createdAt: Timestamp,
    val openDate: Timestamp,
    val status: String
)
