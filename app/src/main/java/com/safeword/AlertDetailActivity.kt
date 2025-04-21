package com.safeword

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.google.firebase.firestore.FirebaseFirestore
import com.example.safeword.model.Alert

class AlertDetailActivity : AppCompatActivity() {
    private lateinit var textAlertInfo: TextView
    private lateinit var textLocation: TextView
    private lateinit var imageView1: ImageView
    private lateinit var imageView2: ImageView
    private lateinit var buttonCallEmergency: Button
    private var alertId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert_detail)
        textAlertInfo = findViewById(R.id.textAlertInfo)
        textLocation = findViewById(R.id.textLocation)
        imageView1 = findViewById(R.id.imageView1)
        imageView2 = findViewById(R.id.imageView2)
        buttonCallEmergency = findViewById(R.id.buttonCallEmergency)
        alertId = intent.getStringExtra("alertId")
        if (alertId == null) {
            finish()
            return
        }
        // Listen for alert document updates
        val db = FirebaseFirestore.getInstance()
        val alertRef = db.collection("alerts").document(alertId!!)
        alertRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
            val alert = snapshot.toObject(Alert::class.java) ?: return@addSnapshotListener
            // Update alert info text
            val urgencyLabel = when (alert.urgency) {
                3 -> getString(R.string.level_high)
                2 -> getString(R.string.level_medium)
                else -> getString(R.string.level_low)
            }
            textAlertInfo.text = getString(R.string.alert_info, alert.triggeredByName, urgencyLabel)
            // Update images if available
            if (alert.imageUrls.isNotEmpty()) {
                imageView1.visibility = View.VISIBLE
                imageView1.load(alert.imageUrls[0])
            }
            if (alert.imageUrls.size > 1) {
                imageView2.visibility = View.VISIBLE
                imageView2.load(alert.imageUrls[1])
            } else {
                imageView2.visibility = View.GONE
            }
            // Update location
            if (alert.lat != null && alert.lng != null) {
                textLocation.text = getString(R.string.location_format, alert.lat, alert.lng)
                textLocation.setOnClickListener {
                    try {
                        val uri = Uri.parse("geo:${alert.lat},${alert.lng}?q=${alert.lat},${alert.lng}(SafeWord Alert)")
                        val mapIntent = Intent(Intent.ACTION_VIEW).apply {
                            data = uri
                        }
                        startActivity(mapIntent)
                    } catch (e: ActivityNotFoundException) {
                        // No map app available
                    }
                }
            } else {
                textLocation.text = getString(R.string.location_unavailable)
            }
        }
        // Handle emergency call trigger
        buttonCallEmergency.setOnClickListener {
            alertId?.let {
                alertRef.update("callRequested", true)
                Toast.makeText(this, R.string.call_requested, Toast.LENGTH_SHORT).show()
                buttonCallEmergency.isEnabled = false
            }
        }
    }
}
