package com.safeword

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.NotificationCompat
import com.safeword.EmergencyHandler.handleEmergencyTrigger

class SafeWordService : Service() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var recognizerIntent: Intent

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceWithNotification()
        initSpeechRecognition()
    }

    private fun startForegroundServiceWithNotification() {
        val channelId = "safeword_listening_channel"
        val channelName = "SafeWord Listening"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("SafeWord Active")
            .setContentText("Listening for emergency safe words")
            .setSmallIcon(R.drawable.ic_safeword_logo)
            .build()

        startForeground(1, notification)
    }

    private fun initSpeechRecognition() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let { phrases ->
                    for (phrase in phrases) {
                        Log.d("SafeWordService", "Heard: $phrase")
                        handleEmergencyTrigger(this@SafeWordService, phrase)
                    }
                }
                restartListening()
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                Log.e("SafeWordService", "SpeechRecognizer error: $error")
                restartListening()
            }
        })
        restartListening()
    }

    private fun restartListening() {
        speechRecognizer.stopListening()
        speechRecognizer.startListening(recognizerIntent)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        speechRecognizer.destroy()
        super.onDestroy()
    }
}
