package com.ecozym.wastemanagement.activities

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ecozym.wastemanagement.R
import com.ecozym.wastemanagement.databinding.ActivityLoginBinding
import com.ecozym.wastemanagement.utils.ValidationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var isPasswordVisible = false

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpStep1Activity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            // TODO: Implement forgot password
            Toast.makeText(this, "Forgot password feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        if (isPasswordVisible) {
            binding.etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.btnTogglePassword.setImageResource(R.drawable.ic_visibility_off)
        } else {
            binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.btnTogglePassword.setImageResource(R.drawable.ic_visibility)
        }
        binding.etPassword.setSelection(binding.etPassword.text.length)
    }

    private fun loginUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (!ValidationUtils.isValidEmail(email)) {
            binding.etEmail.error = "Please enter a valid email"
            return
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Password is required"
            return
        }

        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Logging in..."

        Log.d(TAG, "Attempting login for: $email")

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "Log In"

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        Log.d(TAG, "Login successful for UID: ${it.uid}")
                        checkUserRoleAndRedirect(it.uid)
                    }
                } else {
                    Log.e(TAG, "Login failed", task.exception)
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUserRoleAndRedirect(uid: String) {
        Log.d(TAG, "Checking user role for UID: $uid")

        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role") ?: ""
                    val status = document.getString("status") ?: "pending"

                    Log.d(TAG, "User data - Role: $role, Status: $status")

                    // Check status first
                    when (status) {
                        "pending" -> {
                            Toast.makeText(this, "Your account is pending approval", Toast.LENGTH_LONG).show()
                            auth.signOut()
                            Log.d(TAG, "User signed out - status pending")
                            return@addOnSuccessListener
                        }
                        "rejected" -> {
                            Toast.makeText(this, "Your account has been rejected", Toast.LENGTH_LONG).show()
                            auth.signOut()
                            Log.d(TAG, "User signed out - status rejected")
                            return@addOnSuccessListener
                        }
                    }

                    // Check role - FIXED to include super_admin
                    when (role) {
                        "company" -> {
                            Log.d(TAG, "Redirecting to CompanyMainActivity")
                            startActivity(Intent(this, CompanyMainActivity::class.java))
                            finish()
                        }
                        "progress_admin", "production_admin", "super_admin" -> {
                            Log.d(TAG, "Redirecting to AdminMainActivity for role: $role")
                            startActivity(Intent(this, AdminMainActivity::class.java))
                            finish()
                        }
                        "driver" -> {
                            Log.d(TAG, "Driver role - redirecting to appropriate activity")
                            // Add driver activity navigation when implemented
                            startActivity(Intent(this, CompanyMainActivity::class.java))
                            finish()
                        }
                        else -> {
                            Log.e(TAG, "Invalid or unknown user role: $role")
                            Toast.makeText(this, "Invalid user role: $role", Toast.LENGTH_SHORT).show()
                            auth.signOut()
                        }
                    }
                } else {
                    Log.e(TAG, "User document not found in Firestore")
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    auth.signOut()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to check user data", exception)
                Toast.makeText(this, "Failed to check user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                auth.signOut()
            }
    }
}