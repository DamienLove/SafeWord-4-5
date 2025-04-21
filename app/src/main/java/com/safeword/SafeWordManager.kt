package com.safeword

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class SafeWordManager : AppCompatActivity() {

    private lateinit var safeWordInput: EditText
    private lateinit var urgencySpinner: Spinner
    private lateinit var addButton: Button
    private lateinit var safeWordList: LinearLayout

    private val userId = "demo_user"  // TODO: Replace with real authenticated user ID
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_safe_word_manager)

        safeWordInput = findViewById(R.id.safe_word_input)
        urgencySpinner = findViewById(R.id.urgency_spinner)
        addButton = findViewById(R.id.btn_add_safe_word)
        safeWordList = findViewById(R.id.safe_word_list)

        urgencySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("low", "medium", "high", "life-or-death")
        )

        loadSafeWords()

        addButton.setOnClickListener {
            val word = safeWordInput.text.toString().trim().lowercase()
            val urgency = urgencySpinner.selectedItem.toString()
            if (word.isNotEmpty()) {
                addSafeWord(word, urgency)
                safeWordInput.text.clear()
            }
        }
    }

    private fun loadSafeWords() {
        safeWordList.removeAllViews()
        db.collection("safeWords")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.forEach { doc ->
                    val word = doc.getString("phrase") ?: return@forEach
                    val urgency = doc.getString("urgency") ?: "unknown"
                    val view = layoutInflater.inflate(R.layout.safe_word_item, null)
                    view.findViewById<TextView>(R.id.word_text).text = "\"$word\" → $urgency"
                    view.findViewById<Button>(R.id.btn_delete_word).setOnClickListener {
                        db.collection("safeWords").document(doc.id).delete().addOnSuccessListener {
                            loadSafeWords()
                        }
                    }
                    safeWordList.addView(view)
                }
            }
    }

    private fun addSafeWord(word: String, urgency: String) {
        val newEntry = hashMapOf(
            "phrase" to word,
            "urgency" to urgency,
            "userId" to userId
        )
        db.collection("safeWords").add(newEntry).addOnSuccessListener {
            loadSafeWords()
        }
    }
}
