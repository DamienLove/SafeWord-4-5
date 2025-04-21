package com.safeword

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker

class EmergencyCallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.safeword.ACTION_TRIGGER_CALL") {
            val contactId = intent.getStringExtra("contactId") ?: return
            Log.d("EmergencyCallReceiver", "Trigger call for contact: $contactId")

            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:911") // Placeholder — dynamically inject if needed

            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CALL_PHONE
            )

            if (permissionCheck == PermissionChecker.PERMISSION_GRANTED) {
                callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(callIntent)
            } else {
                Toast.makeText(context, "CALL_PHONE permission not granted", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
