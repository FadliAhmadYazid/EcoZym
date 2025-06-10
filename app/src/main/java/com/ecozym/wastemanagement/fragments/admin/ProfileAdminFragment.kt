package com.ecozym.wastemanagement.fragments.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ecozym.wastemanagement.activities.LoginActivity
import com.ecozym.wastemanagement.databinding.FragmentProfileAdminBinding
import com.ecozym.wastemanagement.viewmodels.AdminViewModel
import com.google.firebase.auth.FirebaseAuth

class ProfileAdminFragment : Fragment() {

    private var _binding: FragmentProfileAdminBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AdminViewModel
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this)[AdminViewModel::class.java]

        setupClickListeners()
        setupObservers()
        loadProfile()
    }

    private fun setupClickListeners() {
        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun setupObservers() {
        viewModel.adminProfile.observe(viewLifecycleOwner) { profile ->
            binding.tvName.text = profile.name ?: "Admin"
            binding.tvEmail.text = profile.email
            binding.tvRole.text = when (profile.adminRole) {
                "super_admin" -> "Super Admin"
                "progress_admin" -> "Progress Admin"
                "production_admin" -> "Production Admin"
                else -> "Admin"
            }

            // Set initials for avatar
            val name = profile.name ?: profile.email
            val initials = name.split(" ", "@").take(2).joinToString("") {
                if (it.isNotEmpty()) it.first().toString() else ""
            }
            binding.tvInitials.text = initials.uppercase()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // TODO: Show loading indicator
        }
    }

    private fun loadProfile() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            viewModel.loadAdminProfile(user.uid)
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                logout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun logout() {
        auth.signOut()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
