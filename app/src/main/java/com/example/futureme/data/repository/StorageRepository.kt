package com.example.futureme.data.repository

import android.content.Context
import android.net.Uri
import com.example.futureme.data.firebase.StorageDataSource
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class StorageRepository(
    private val dataSource: StorageDataSource = StorageDataSource()
) {

    suspend fun uploadImages(
        context: Context,
        userId: String,
        uris: List<Uri>
    ): List<String> {
        if (uris.isEmpty()) return emptyList()

        return dataSource.uploadImages(context, userId, uris)
    }

    suspend fun deleteImagesByUrls(urls: List<String>) {
        val storage = FirebaseStorage.getInstance()
        urls.distinct().forEach { url ->
            try {
                storage.getReferenceFromUrl(url).delete().await()
            } catch (_: Exception) {
            }
        }
    }
}
