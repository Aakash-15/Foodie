package com.example.foodie

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // Display Views
    private lateinit var userNameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var addressTextView: TextView

    // Edit Views
    private lateinit var userNameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var addressEditText: EditText

    // Buttons
    private lateinit var editProfileButton: Button
    private lateinit var saveProfileButton: Button
    private lateinit var logoutButton: Button

    private val TAG = "ProfileActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initializeViews()
        loadUserProfile()

        editProfileButton.setOnClickListener {
            setEditMode(true)
        }

        saveProfileButton.setOnClickListener {
            saveUserProfile()
        }

        logoutButton.setOnClickListener {
            // Log out the user
            auth.signOut()

            // Clear the "Remember Me" preference
            val sharedPreferences = getSharedPreferences("FoodiePrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("isLoggedIn", false)
            editor.apply()

            // Go to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun initializeViews() {
        userNameTextView = findViewById(R.id.userNameTextView)
        emailTextView = findViewById(R.id.emailTextView)
        phoneTextView = findViewById(R.id.phoneTextView)
        addressTextView = findViewById(R.id.addressTextView)
        userNameEditText = findViewById(R.id.userNameEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        addressEditText = findViewById(R.id.addressEditText)
        editProfileButton = findViewById(R.id.editProfileButton)
        saveProfileButton = findViewById(R.id.saveProfileButton)
        logoutButton = findViewById(R.id.logoutButton)
    }

    private fun setEditMode(isEditing: Boolean) {
        userNameTextView.visibility = if (isEditing) View.GONE else View.VISIBLE
        phoneTextView.visibility = if (isEditing) View.GONE else View.VISIBLE
        addressTextView.visibility = if (isEditing) View.GONE else View.VISIBLE
        editProfileButton.visibility = if (isEditing) View.GONE else View.VISIBLE

        userNameEditText.visibility = if (isEditing) View.VISIBLE else View.GONE
        phoneEditText.visibility = if (isEditing) View.VISIBLE else View.GONE
        addressEditText.visibility = if (isEditing) View.VISIBLE else View.GONE
        saveProfileButton.visibility = if (isEditing) View.VISIBLE else View.GONE

        if (isEditing) {
            userNameEditText.setText(if (userNameTextView.text.contains("not found")) "" else userNameTextView.text)
            phoneEditText.setText(if (phoneTextView.text.contains("not found")) "" else phoneTextView.text)
            addressEditText.setText(if (addressTextView.text.contains("not found")) "" else addressTextView.text)
        }
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "User not logged in, cannot load profile.")
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val userDocRef = firestore.collection("user_detail").document(userId)
        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    Log.d(TAG, "User profile data found: ${document.data}")

                    val name = document.getString("name")
                    val email = document.getString("email")
                    val phone = document.getString("phone")
                    val address = document.getString("address")
                    
                    userNameTextView.text = name ?: "Name not found"
                    emailTextView.text = email ?: "Email not found"
                    phoneTextView.text = phone ?: "Phone not found"
                    addressTextView.text = address ?: "Address not found"

                } else {
                    Log.d(TAG, "No profile document found for user: $userId")
                    Toast.makeText(this, "No profile data found. Please edit to create one.", Toast.LENGTH_LONG).show()
                    // Set placeholder text if no document exists
                    userNameTextView.text = "No data"
                    emailTextView.text = "No data"
                    phoneTextView.text = "No data"
                    addressTextView.text = "No data"
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to load user profile.", e)
                Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val name = userNameEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()
        val address = addressEditText.text.toString().trim()

        if (name.isEmpty()) {
            userNameEditText.error = "Name cannot be empty"
            userNameEditText.requestFocus()
            return
        }

        val userUpdates = hashMapOf(
            "name" to name,
            "phone" to phone,
            "address" to address
        )

        val userDocRef = firestore.collection("user_detail").document(userId)
        userDocRef.update(userUpdates as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                userNameTextView.text = name
                phoneTextView.text = phone
                addressTextView.text = address
                setEditMode(false)
            }
            .addOnFailureListener { e ->
                 if (e.message?.contains("NOT_FOUND") == true) {
                    // If the document doesn't exist, create it instead of updating
                    userDocRef.set(userUpdates)
                        .addOnSuccessListener {
                             Toast.makeText(this, "Profile created successfully", Toast.LENGTH_SHORT).show()
                             userNameTextView.text = name
                             phoneTextView.text = phone
                             addressTextView.text = address
                             setEditMode(false)
                        }
                        .addOnFailureListener { e2 ->
                            Log.e(TAG, "Failed to create profile.", e2)
                            Toast.makeText(this, "Failed to create profile: ${e2.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Log.e(TAG, "Failed to update profile.", e)
                    Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
