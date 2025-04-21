package com.safeword

import android.Manifest
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class RecordVoiceActivity() : AppCompatActivity(), Parcelable {

    private lateinit var safeWordInput: EditText
    private lateinit var recordButton: Button
    private var mediaRecorder: MediaRecorder? = null

    constructor(parcel: Parcel) : this() {

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RecordVoiceActivity> {
        override fun createFromParcel(parcel: Parcel): RecordVoiceActivity {
            return RecordVoiceActivity(parcel)
        }

        override fun newArray(size: Int): Array<RecordVoiceActivity?> {
            return arrayOfNulls(size)
        }
    }

}
