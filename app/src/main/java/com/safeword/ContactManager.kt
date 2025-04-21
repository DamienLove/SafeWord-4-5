package com.safeword

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class ContactManager : AppCompatActivity() {

    private lateinit var contactsLayout: LinearLayout
    private lateinit var contactInput: EditText
    private lateinit var addButton: Button
    private val db = FirebaseFirestore.getInstance()
    private val userId = "demo_user" // Replace with real user ID when implementing auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_manager)

        contactsLayout = findViewById(R.id.contacts_layout)
        contactInput = findViewById(R.id.contact_input)
        addButton = findViewById(R.id.btn_add_contact)

        loadContacts()

        addButton.setOnClickListener {
            val contact = contactInput.text.toString()
            if (contact.isNotBlank()) {
                addContact(contact)
                contactInput.text.clear()
            }
        }
    }

    private fun loadContacts() {
        db.collection("users").document(userId)
            .collection("contacts")
            .get()
            .addOnSuccessListener { snapshot ->
                contactsLayout.removeAllViews()
                snapshot.forEach { doc ->
                    val contact = doc.getString("contactId") ?: return@forEach
                    val view = layoutInflater.inflate(R.layout.contact_list_item, null)
                    view.findViewById<TextView>(R.id.contact_name).text = contact
                    view.findViewById<Button>(R.id.btn_remove_contact).setOnClickListener {
                        removeContact(doc.id)
                    }
                    contactsLayout.addView(view)
                }
            }
    }

    private fun addContact(contactId: String) {
        val data = hashMapOf("contactId" to contactId, "priority" to System.currentTimeMillis())
        db.collection("users").document(userId)
            .collection("contacts")
            .add(data)
            .addOnSuccessListener { loadContacts() }
    }

    private fun removeContact(docId: String) {
        db.collection("users").document(userId)
            .collection("contacts").document(docId)
            .delete()
            .addOnSuccessListener { loadContacts() }
    }
}
