package com.safeword

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var startListeningButton: Button
    private lateinit var manageContactsButton: Button
    private lateinit var manageSafeWordsButton: Button

    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.MODIFY_AUDIO_SETTINGS
    )

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val allGranted = permissionsMap.all { it.value }
        if (allGranted) {
            startVoiceService()
        } else {
            Toast.makeText(this, "All permissions must be granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 🔐 Check login first
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        startListeningButton = findViewById(R.id.btn_start_listening)
        manageContactsButton = findViewById(R.id.btn_manage_contacts)
        manageSafeWordsButton = findViewById(R.id.btn_manage_safe_words)

        startListeningButton.setOnClickListener {
            if (hasPermissions()) {
                startVoiceService()
            } else {
                requestPermissions.launch(permissions)
            }
        }

        manageContactsButton.setOnClickListener {
            startActivity(Intent(this, ContactManager::class.java))
        }

        manageSafeWordsButton.setOnClickListener {
            startActivity(Intent(this, SafeWordManager::class.java))
        }
    }

    private fun hasPermissions(): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun startVoiceService() {
        val intent = Intent(this, SafeWordService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        Toast.makeText(this, "SafeWord is now listening...", Toast.LENGTH_SHORT).show()
    }
}
