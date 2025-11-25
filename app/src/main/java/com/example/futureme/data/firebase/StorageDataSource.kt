package com.example.futureme.data.firebase

import com.google.firebase.storage.FirebaseStorage

class StorageDataSource(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
}
