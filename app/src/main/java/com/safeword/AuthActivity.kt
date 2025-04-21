package com.safeword

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class AuthActivity : AppCompatActivity() {
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var editName: EditText
    private lateinit var buttonAuth: Button
    private lateinit var textToggleMode: TextView
    private var isSignUpMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        editEmail = findViewById(R.id.editEmail)
        editPassword = findViewById(R.id.editPassword)
        editName = findViewById(R.id.editName)
        buttonAuth = findViewById(R.id.buttonAuth)
        textToggleMode = findViewById(R.id.textToggleMode)
        // Toggle between Sign In and Sign Up mode
        textToggleMode.setOnClickListener {
            isSignUpMode = !isSignUpMode
            if (isSignUpMode) {
                buttonAuth.text = getString(R.string.action_sign_up)
                textToggleMode.text = getString(R.string.toggle_to_sign_in)
                editName.visibility = EditText.VISIBLE
            } else {
                buttonAuth.text = getString(R.string.action_sign_in)
                textToggleMode.text = getString(R.string.toggle_to_sign_up)
                editName.visibility = EditText.GONE
            }
        }
        // Handle authentication button click
        buttonAuth.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()
            val name = editName.text.toString().trim()
            if (email.isEmpty() || password.isEmpty() || (isSignUpMode && name.isEmpty())) {
                Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (isSignUpMode) {
                // Sign up new user
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = FirebaseAuth.getInstance().currentUser
                            if (user != null) {
                                // Update display name
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setDisplayName(name).build()
                                user.updateProfile(profileUpdates)
                                // Save user profile in Firestore
                                val db = FirebaseFirestore.getInstance()
                                val userMap = hashMapOf<String, Any>(
                                    "email" to email,
                                    "name" to name
                                )
                                db.collection("users").document(user.uid).set(userMap)
                            }
                            navigateToMain(FirebaseAuth.getInstance().currentUser)
                        } else {
                            Toast.makeText(this, task.exception?.message ?: "Sign up failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                // Sign in existing user
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            navigateToMain(FirebaseAuth.getInstance().currentUser)
                        } else {
                            Toast.makeText(this, task.exception?.message ?: "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun navigateToMain(user: FirebaseUser?) {
        if (user != null) {
            // Go to MainActivity
            MainActivity.start(this)
            finish()
        }
    }
}
