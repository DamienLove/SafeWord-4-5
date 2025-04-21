package com.safeword

import android.Manifest
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class RecordVoiceActivity : AppCompatActivity() {

    private lateinit var safeWordInput: EditText
    private lateinit var recordButton: Button
    private var mediaRecorder: MediaRecorder? = null
    private
