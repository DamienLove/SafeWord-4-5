package com.safeword

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class SafeWordApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }

    companion object {
        val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
        val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    }
}
