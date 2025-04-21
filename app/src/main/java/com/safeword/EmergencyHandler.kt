package com.safeword

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object EmergencyHandler {

    fun handleEmergencyTrigger(context: Context, phrase: String) {
        val safeWordsRef = FirebaseFirestore.getInstance().collection("safeWords")
        safeWordsRef.whereEqualTo("phrase", phrase)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    for (document in result) {
                        val urgency = document.getString("urgency") ?: "unknown"
                        val userId = document.getString("userId") ?: ""
                        Log.d("EmergencyHandler", "Safe word matched with urgency: $urgency")
                        triggerEmergencyProtocol(context, userId, urgency)
                        break
                    }
                } else {
                    Log.d("EmergencyHandler", "No safe word match for: $phrase")
                }
            }
            .addOnFailureListener {
                Log.e("EmergencyHandler", "Failed to query safe words: ${it.message}")
            }
    }

    private fun triggerEmergencyProtocol(context: Context, userId: String, urgency: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val location = LocationHelper.getLastKnownLocation(context)
            val photoUri = MediaCapture.capturePhoto(context)
            val videoUri = MediaCapture.captureVideo(context)
            val contactId = ContactManager.getFirstContactId(userId)

            val data = hashMapOf(
                "userId" to userId,
                "urgency" to urgency,
                "timestamp" to getTimestamp(),
                "location" to location?.let { "${it.latitude},${it.longitude}" },
                "photoUrl" to photoUri.toString(),
                "videoUrl" to videoUri.toString(),
                "contactId" to contactId
            )

            FirebaseFirestore.getInstance().collection("emergencies")
                .add(data)
                .addOnSuccessListener {
                    Log.d("EmergencyHandler", "Emergency data sent.")
                    notifyContactApp(context, contactId)
                }
                .addOnFailureListener {
                    Log.e("EmergencyHandler", "Error saving emergency: ${it.message}")
                }
        }
    }

    private fun notifyContactApp(context: Context, contactId: String) {
        val intent = Intent(context, EmergencyCallReceiver::class.java)
        intent.action = "com.safeword.ACTION_TRIGGER_CALL"
        intent.putExtra("contactId", contactId)
        context.sendBroadcast(intent)
    }

    private fun getTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}
