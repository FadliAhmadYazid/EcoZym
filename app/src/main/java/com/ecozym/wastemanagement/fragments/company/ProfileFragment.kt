package com.ecozym.wastemanagement.fragments.company

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ecozym.wastemanagement.activities.LoginActivity
import com.ecozym.wastemanagement.databinding.FragmentProfileBinding
import com.ecozym.wastemanagement.viewmodels.CompanyViewModel
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CompanyViewModel
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this)[CompanyViewModel::class.java]

        setupClickListeners()
        setupObservers()
        loadProfile()
    }

    private fun setupClickListeners() {
        binding.btnEditProfile.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.ecozym.wastemanagement.R.id.fragmentContainer, EditProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun setupObservers() {
        viewModel.companyProfile.observe(viewLifecycleOwner) { profile ->
            binding.tvCompanyName.text = profile.companyName
            binding.tvPhoneNumber.text = profile.phoneNumber
            binding.tvEmail.text = profile.email
            binding.tvAddress.text = profile.address

            // Set initials for avatar
            val initials = profile.companyName.split(" ").take(2).joinToString("") { it.first().toString() }
            binding.tvInitials.text = initials.uppercase()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // TODO: Show loading indicator
        }
    }

    private fun loadProfile() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            viewModel.loadCompanyProfile(user.uid)
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