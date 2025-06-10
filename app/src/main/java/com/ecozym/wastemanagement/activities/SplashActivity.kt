package com.ecozym.wastemanagement.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ecozym.wastemanagement.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    companion object {
        private const val TAG = "SplashActivity"
        private const val SPLASH_DELAY = 2000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({
            checkUserStatus()
        }, SPLASH_DELAY)
    }

    private fun checkUserStatus() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            Log.d(TAG, "User is logged in: ${currentUser.uid}")

            // Check user data in Firestore
            firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val role = document.getString("role") ?: ""
                        val status = document.getString("status") ?: "pending"

                        Log.d(TAG, "User role: $role, status: $status")

                        // Check user status first
                        when (status) {
                            "pending" -> {
                                // User registration is pending approval
                                Log.d(TAG, "User status is pending, signing out")
                                auth.signOut()
                                navigateToOpeningScreen()
                                return@addOnSuccessListener
                            }
                            "rejected" -> {
                                // User registration was rejected
                                Log.d(TAG, "User status is rejected, signing out")
                                auth.signOut()
                                navigateToOpeningScreen()
                                return@addOnSuccessListener
                            }
                            "approved", "active" -> {
                                // User is approved, check role
                                when (role) {
                                    "company" -> {
                                        Log.d(TAG, "Navigating to CompanyMainActivity")
                                        startActivity(Intent(this, CompanyMainActivity::class.java))
                                    }
                                    "admin", "super_admin" -> {
                                        Log.d(TAG, "Navigating to AdminMainActivity")
                                        startActivity(Intent(this, AdminMainActivity::class.java))
                                    }
                                    "driver" -> {
                                        Log.d(TAG, "Driver role - navigating to appropriate activity")
                                        // Add driver activity navigation when implemented
                                        startActivity(Intent(this, CompanyMainActivity::class.java))
                                    }
                                    else -> {
                                        Log.d(TAG, "Unknown role or empty role, going to opening screen")
                                        navigateToOpeningScreen()
                                        return@addOnSuccessListener
                                    }
                                }
                            }
                            else -> {
                                Log.d(TAG, "Unknown status: $status, going to opening screen")
                                navigateToOpeningScreen()
                                return@addOnSuccessListener
                            }
                        }
                        finish()
                    } else {
                        Log.d(TAG, "User document doesn't exist, signing out")
                        auth.signOut()
                        navigateToOpeningScreen()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting user document", exception)

                    // If there's a permission error or any other error,
                    // sign out and go to opening screen
                    auth.signOut()
                    navigateToOpeningScreen()
                }
        } else {
            Log.d(TAG, "No user logged in, going to opening screen")
            navigateToOpeningScreen()
        }
    }

    private fun navigateToOpeningScreen() {
        startActivity(Intent(this, OpeningScreenActivity::class.java))
        finish()
    }
}