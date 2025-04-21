package com.safeword

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SafeWordSetupActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var editSafeWord: EditText
    private lateinit var spinnerUrgency: Spinner
    private lateinit var buttonAdd: Button
    private val safeWordsDisplayList = ArrayList<String>()
    private lateinit var safeWordsAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_safe_word_setup)
        listView = findViewById(R.id.listSafeWords)
        editSafeWord = findViewById(R.id.editSafeWord)
        spinnerUrgency = findViewById(R.id.spinnerUrgency)
        buttonAdd = findViewById(R.id.buttonAddSafeWord)
        // Setup urgency spinner values
        val levels = resources.getStringArray(R.array.urgency_levels)
        spinnerUrgency.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, levels)
        // Setup list adapter
        safeWordsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, safeWordsDisplayList)
        listView.adapter = safeWordsAdapter
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        val safeWordsRef = db.collection("users").document(user.uid).collection("safeWords")
        // Listen for safe words changes in Firestore
        safeWordsRef.addSnapshotListener { snapshots, error ->
            if (error != null) return@addSnapshotListener
            safeWordsDisplayList.clear()
            if (snapshots != null) {
                for (doc in snapshots.documents) {
                    val phrase = doc.getString("phrase") ?: ""
                    val urgency = (doc.getLong("urgency") ?: 1).toInt()
                    val urgencyLabel = when (urgency) {
                        3 -> getString(R.string.level_high)
                        2 -> getString(R.string.level_medium)
                        else -> getString(R.string.level_low)
                    }
                    safeWordsDisplayList.add("$phrase (Urgency: $urgencyLabel)")
                }
            }
            safeWordsAdapter.notifyDataSetChanged()
        }
        // Add new safe word
        buttonAdd.setOnClickListener {
            val phrase = editSafeWord.text.toString().trim()
            if (phrase.isEmpty()) {
                Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val levelIndex = spinnerUrgency.selectedItemPosition
            val urgency = when (levelIndex) {
                0 -> 1; 1 -> 2; else -> 3
            }
            val safeWordData = hashMapOf(
                "phrase" to phrase,
                "urgency" to urgency
            )
            safeWordsRef.add(safeWordData).addOnSuccessListener {
                Toast.makeText(this, R.string.safe_word_added, Toast.LENGTH_SHORT).show()
                editSafeWord.text.clear()
                spinnerUrgency.setSelection(0)
            }.addOnFailureListener {
                Toast.makeText(this, R.string.error_operation_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
