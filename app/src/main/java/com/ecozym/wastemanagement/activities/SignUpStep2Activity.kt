package com.ecozym.wastemanagement.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.ecozym.wastemanagement.R
import com.ecozym.wastemanagement.databinding.ActivitySignupStep2Binding
import com.ecozym.wastemanagement.utils.SignUpData

class SignUpStep2Activity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupStep2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupStep2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinner()
        setupClickListeners()
    }

    private fun setupSpinner() {
        val industryTypes = arrayOf(
            "Food Processing",
            "Fruit Processing",
            "Vegetable Processing",
            "Beverage Industry",
            "Restaurant",
            "Catering",
            "Other"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, industryTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerIndustryType.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnPrevious.setOnClickListener {
            finish()
        }

        binding.btnNext.setOnClickListener {
            validateAndProceed()
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateAndProceed() {
        val companyName = binding.etCompanyName.text.toString().trim()
        val industryType = binding.spinnerIndustryType.selectedItem.toString()
        val address = binding.etCompanyAddress.text.toString().trim()
        val countryCode = binding.etCountryCode.text.toString().trim()
        val phoneNumber = binding.etPhoneNumber.text.toString().trim()

        if (companyName.isEmpty()) {
            binding.etCompanyName.error = "Company name is required"
            return
        }

        if (address.isEmpty()) {
            binding.etCompanyAddress.error = "Company address is required"
            return
        }

        if (phoneNumber.isEmpty()) {
            binding.etPhoneNumber.error = "Phone number is required"
            return
        }

        // Save data and proceed to next step
        SignUpData.companyName = companyName
        SignUpData.industryType = industryType
        SignUpData.address = address
        SignUpData.phoneNumber = "$countryCode$phoneNumber"

        val intent = Intent(this, SignUpStep3Activity::class.java)
        startActivity(intent)
    }
}
