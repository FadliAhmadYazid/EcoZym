package com.ecozym.wastemanagement.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ecozym.wastemanagement.R
import com.ecozym.wastemanagement.databinding.ActivityCompanyMainBinding
import com.ecozym.wastemanagement.fragments.company.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CompanyMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompanyMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompanyMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
        setupHeaderButtons()

        // Set default fragment
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.navHome.setOnClickListener {
            replaceFragment(HomeFragment())
        }

        binding.bottomNavigation.navTrash.setOnClickListener {
            replaceFragment(RegisterWasteFragment())
        }

        binding.bottomNavigation.navTruck.setOnClickListener {
            replaceFragment(WasteTrackingFragment())
        }

        binding.bottomNavigation.navChart.setOnClickListener {
            replaceFragment(ReportFragment())
        }

        binding.bottomNavigation.navArticle.setOnClickListener {
            replaceFragment(GuideFragment())
        }
    }

    private fun setupHeaderButtons() {
        binding.headerLayout.btnNotification.setOnClickListener {
            replaceFragment(NotificationFragment())
        }

        binding.headerLayout.btnProfile.setOnClickListener {
            replaceFragment(ProfileFragment())
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}