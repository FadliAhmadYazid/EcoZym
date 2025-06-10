package com.ecozym.wastemanagement.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ecozym.wastemanagement.databinding.ActivityOpeningScreenBinding

class OpeningScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOpeningScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpeningScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSignIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnCreateAccount.setOnClickListener {
            startActivity(Intent(this, SignUpStep1Activity::class.java))
        }
    }
}
