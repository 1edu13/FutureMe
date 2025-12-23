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
    // Nueva fecha límite para dejar de aceptar contribuciones
    val editDeadline: Timestamp = Timestamp.now(), 
    val status: String = "",
    val images: List<String> = emptyList(),
    val contributions: Map<String, Map<String, Any>> = emptyMap()
) {
    // ¿Se puede ver el contenido?
    fun isOpenable(): Boolean {
        return Date() >= openDate.toDate()
    }

    // ¿Se puede editar/añadir contenido? (Solo si NO ha pasado la fecha límite)
    fun isEditable(): Boolean {
        return Date() < editDeadline.toDate()
    }

    fun canJoin(): Boolean = Date() < editDeadline.toDate()
}
