package com.example.futureme.data.firebase

import com.google.firebase.firestore.FirebaseFirestore

class CapsuleDataSource(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
}
