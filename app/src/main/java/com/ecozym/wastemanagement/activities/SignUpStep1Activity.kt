package com.ecozym.wastemanagement.activities

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import com.ecozym.wastemanagement.R
import com.ecozym.wastemanagement.databinding.ActivitySignupStep1Binding
import com.ecozym.wastemanagement.utils.SignUpData
import com.ecozym.wastemanagement.utils.ValidationUtils

class SignUpStep1Activity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupStep1Binding
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupStep1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        binding.btnToggleConfirmPassword.setOnClickListener {
            toggleConfirmPasswordVisibility()
        }

        binding.btnNext.setOnClickListener {
            validateAndProceed()
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
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

    private fun toggleConfirmPasswordVisibility() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible
        if (isConfirmPasswordVisible) {
            binding.etConfirmPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.btnToggleConfirmPassword.setImageResource(R.drawable.ic_visibility_off)
        } else {
            binding.etConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.btnToggleConfirmPassword.setImageResource(R.drawable.ic_visibility)
        }
        binding.etConfirmPassword.setSelection(binding.etConfirmPassword.text.length)
    }

    private fun validateAndProceed() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (!ValidationUtils.isValidEmail(email)) {
            binding.etEmail.error = "Please enter a valid email"
            return
        }

        if (!ValidationUtils.isValidPassword(password)) {
            binding.etPassword.error = "Password must be at least 8 characters"
            return
        }

        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Passwords do not match"
            return
        }

        // Save data and proceed to next step
        SignUpData.email = email
        SignUpData.password = password

        val intent = Intent(this, SignUpStep2Activity::class.java)
        startActivity(intent)
    }
}