package com.ecozym.wastemanagement.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ecozym.wastemanagement.R
import com.ecozym.wastemanagement.databinding.ActivityAdminMainBinding
import com.ecozym.wastemanagement.fragments.admin.*
import com.ecozym.wastemanagement.viewmodels.NotificationViewModel
import com.example.wastewiseapp.ui.production.ProductionManagementFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdminMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminMainBinding
    private lateinit var notificationViewModel: NotificationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize NotificationViewModel
        notificationViewModel = ViewModelProvider(this)[NotificationViewModel::class.java]

        setupBottomNavigation()
        setupHeaderButtons()
        setupNotificationObserver()

        // Set default fragment
        if (savedInstanceState == null) {
            replaceFragment(DashboardFragment())
        }

        // Load notification count
        loadNotificationCount()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.navHome.setOnClickListener {
            replaceFragment(DashboardFragment())
        }

        binding.bottomNavigation.navProgress.setOnClickListener {
            replaceFragment(ProgressManagementFragment())
        }

        binding.bottomNavigation.navProduction.setOnClickListener {
            replaceFragment(ProductionManagementFragment())
        }

        binding.bottomNavigation.navPartner.setOnClickListener {
            replaceFragment(PartnerManagementFragment())
        }

        binding.bottomNavigation.navReport.setOnClickListener {
            replaceFragment(ReportAdminFragment())
        }
    }

    private fun setupHeaderButtons() {
        binding.headerLayout.btnNotification.setOnClickListener {
            replaceFragment(NotificationAdminFragment())
            // Reset notification badge ketika dibuka
            updateNotificationBadge(0)
        }

        binding.headerLayout.btnProfile.setOnClickListener {
            replaceFragment(ProfileAdminFragment())
        }
    }

    private fun setupNotificationObserver() {
        // Observe unread notification count
        notificationViewModel.unreadCount.observe(this) { count ->
            updateNotificationBadge(count)
        }
    }

    private fun updateNotificationBadge(count: Int) {
        // Update notification button appearance berdasarkan unread count
        if (count > 0) {
            // Ubah warna button ke orange untuk indikasi ada notifikasi
            binding.headerLayout.btnNotification.setColorFilter(
                android.graphics.Color.parseColor("#FF5722") // Orange untuk ada notifikasi
            )
        } else {
            // Reset ke warna normal
            binding.headerLayout.btnNotification.clearColorFilter()
        }
    }

    private fun loadNotificationCount() {
        notificationViewModel.loadAdminNotifications()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        // Refresh notification count when activity resumes
        loadNotificationCount()
    }
}