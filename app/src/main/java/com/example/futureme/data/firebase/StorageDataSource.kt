package com.example.futureme.data.firebase

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StorageDataSource(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    suspend fun uploadImages(context: Context, userId: String, uris: List<Uri>): List<String> {
        val urls = mutableListOf<String>()
        val resolver = context.contentResolver

        for (uri in uris) {

            val inputStream = resolver.openInputStream(uri)
                ?: throw Exception("No se pudo abrir InputStream para $uri")

            val bytes = inputStream.readBytes()
            inputStream.close()

            val fileName = "capsules/$userId/${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child(fileName)

            ref.putBytes(bytes).await()
            val downloadUrl = ref.downloadUrl.await().toString()

            urls.add(downloadUrl)
        }

        return urls
    }
}
