package com.safeword

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ContactsActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var editFriendEmail: EditText
    private lateinit var buttonAddFriend: Button
    private val contactsList = ArrayList<String>()
    private lateinit var contactsAdapter: android.widget.ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)
        listView = findViewById(R.id.listContacts)
        editFriendEmail = findViewById(R.id.editFriendEmail)
        buttonAddFriend = findViewById(R.id.buttonAddFriend)
        contactsAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_list_item_1, contactsList)
        listView.adapter = contactsAdapter
        // Load existing contacts from Firestore
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        val contactsRef = db.collection("users").document(user.uid).collection("contacts")
        contactsRef.addSnapshotListener { snapshots, error ->
            if (error != null) return@addSnapshotListener
            contactsList.clear()
            if (snapshots != null) {
                for (doc in snapshots.documents) {
                    val name = doc.getString("friendName") ?: ""
                    val email = doc.getString("friendEmail") ?: ""
                    val display = if (name.isNotEmpty()) "$name ($email)" else email
                    contactsList.add(display)
                }
            }
            contactsAdapter.notifyDataSetChanged()
        }
        // Add new friend contact
        buttonAddFriend.setOnClickListener {
            val friendEmail = editFriendEmail.text.toString().trim()
            if (friendEmail.isEmpty()) {
                Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (friendEmail == user.email) {
                Toast.makeText(this, R.string.error_add_self, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Find friend by email in users collection
            db.collection("users").whereEqualTo("email", friendEmail).get()
                .addOnSuccessListener { query ->
                    if (query.documents.isEmpty()) {
                        Toast.makeText(this, R.string.error_user_not_found, Toast.LENGTH_SHORT).show()
                    } else {
                        val friendDoc = query.documents[0]
                        val friendId = friendDoc.id
                        val friendName = friendDoc.getString("name") ?: ""
                        val friendEmailCanonical = friendDoc.getString("email") ?: friendEmail
                        // Add friend to current user's contacts subcollection
                        val contactData = hashMapOf<String, Any>(
                            "friendId" to friendId,
                            "friendName" to friendName,
                            "friendEmail" to friendEmailCanonical,
                            "addedAt" to System.currentTimeMillis()
                        )
                        contactsRef.document(friendId).set(contactData).addOnSuccessListener {
                            Toast.makeText(this, R.string.friend_added, Toast.LENGTH_SHORT).show()
                            editFriendEmail.text.clear()
                        }
                    }
                }
        }
    }
}
