package com.safeword

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data.isNotEmpty().let {
            val data = remoteMessage.data
            if (data.containsKey("alertId") && data.containsKey("triggeredByName")) {
                // Notification for friend about a new alert
                val alertId = data["alertId"]
                val triggeredByName = data["triggeredByName"]
                val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                // Create channel if not exists
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(SafeWordService.CHANNEL_ID_ALERT, "SafeWord Alerts", NotificationManager.IMPORTANCE_HIGH)
                    notificationManager.createNotificationChannel(channel)
                }
                // Build notification
                val intent = Intent(this, AlertDetailActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("alertId", alertId)
                }
                val pendingIntent = androidx.core.app.TaskStackBuilder.create(this).run {
                    addNextIntentWithParentStack(intent)
                    getPendingIntent(0, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) androidx.core.app.PendingIntent.FLAG_IMMUTABLE or androidx.core.app.PendingIntent.FLAG_UPDATE_CURRENT else androidx.core.app.PendingIntent.FLAG_UPDATE_CURRENT)
                }
                val notification = NotificationCompat.Builder(this, SafeWordService.CHANNEL_ID_ALERT)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.notification_alert_received, triggeredByName))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                    .setContentIntent(pendingIntent)
                    .build()
                notificationManager.notify(100, notification)
            } else if (data.containsKey("action") && data["action"] == "call") {
                // Notification data instructing original phone to call emergency
                val intent = Intent(Intent.ACTION_CALL, android.net.Uri.parse("tel:911"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }
}
