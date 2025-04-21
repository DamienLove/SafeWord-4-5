package com.safeword

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MediaCapture {

    fun capturePhoto(context: Context): Uri {
        // Simulated placeholder - replace with real camera capture if needed
        return Uri.parse("android.resource://${context.packageName}/drawable/ic_safeword_logo")
    }

    fun captureVideo(context: Context): Uri {
        // Simulated placeholder - replace with real video capture if needed
        return Uri.parse("android.resource://${context.packageName}/drawable/ic_safeword_logo")
    }

    suspend fun uploadToFirebase(uri: Uri, fileType: String): String? {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "$fileType-$timestamp"
            val ref = FirebaseStorage.getInstance().reference.child("media/$filename")
            val uploadTask = ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("MediaCapture", "Upload failed: ${e.message}")
            null
        }
    }

    fun getPlaceholderUri(): Uri {
        return Uri.parse("android.resource://com.safeword/drawable/ic_safeword_logo")
    }
}
