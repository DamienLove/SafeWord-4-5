package com.safeword

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            // Initialize Firebase and start service if user is logged in
            FirebaseApp.initializeApp(context)
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val serviceIntent = Intent(context, SafeWordService::class.java)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
}
