package com.ecozym.wastemanagement.fragments.company

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ecozym.wastemanagement.databinding.FragmentEditProfileBinding
import com.ecozym.wastemanagement.models.Company
import com.ecozym.wastemanagement.utils.CloudinaryHelper
import com.ecozym.wastemanagement.viewmodels.CompanyViewModel
import com.google.firebase.auth.FirebaseAuth

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CompanyViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var cloudinaryHelper: CloudinaryHelper
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                // Display selected image preview
                binding.ivProfileImage.setImageURI(uri)
                binding.tvInitials.visibility = View.GONE
                binding.ivProfileImage.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this)[CompanyViewModel::class.java]
        cloudinaryHelper = CloudinaryHelper()

        setupClickListeners()
        setupObservers()
        loadProfile()
    }

    private fun setupClickListeners() {
        binding.btnChangePhoto.setOnClickListener {
            selectImage()
        }

        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnSaveChanges.setOnClickListener {
            saveChanges()
        }
    }

    private fun setupObservers() {
        viewModel.companyProfile.observe(viewLifecycleOwner) { profile ->
            populateFields(profile)
        }

        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = {
                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                },
                onFailure = { error ->
                    Toast.makeText(context, "Failed to update profile: ${error.message}", Toast.LENGTH_SHORT).show()
                },
                onLoading = {
                    // Handle loading state - show loading indicator
                    // You can add a progress bar to your layout and show it here
                    // binding.progressBar.visibility = View.VISIBLE
                    // Or disable buttons to prevent multiple clicks
                    // binding.fabAddPartner.isEnabled = false
                }
            )
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnSaveChanges.isEnabled = !isLoading
            binding.btnSaveChanges.text = if (isLoading) "Saving..." else "Save Changes"
        }
    }

    private fun populateFields(profile: Company) {
        binding.etCompanyName.setText(profile.companyName)
        binding.etPhoneNumber.setText(profile.phoneNumber)
        binding.etEmail.setText(profile.email)
        binding.etAddress.setText(profile.address)
        binding.etCompanyType.setText(profile.industryType ?: "")
        binding.etBusinessLicense.setText(profile.businessLicense ?: "")

        // Set profile image or initials
        if (profile.profileImageUrl != null) {
            // TODO: Load image from URL using image loading library (Glide/Picasso)
            // For now, show initials
            setInitials(profile.companyName)
        } else {
            setInitials(profile.companyName)
        }
    }

    private fun setInitials(companyName: String) {
        val initials = companyName.split(" ")
            .take(2)
            .joinToString("") { word ->
                if (word.isNotEmpty()) word.first().toString() else ""
            }
        binding.tvInitials.text = initials.uppercase()
        binding.tvInitials.visibility = View.VISIBLE
        binding.ivProfileImage.visibility = View.GONE
    }

    private fun loadProfile() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            viewModel.loadCompanyProfile(user.uid)
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        imagePickerLauncher.launch(intent)
    }

    private fun saveChanges() {
        if (!validateFields()) {
            return
        }

        val currentUser = auth.currentUser ?: return

        val companyName = binding.etCompanyName.text.toString().trim()
        val phoneNumber = binding.etPhoneNumber.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val companyType = binding.etCompanyType.text.toString().trim()
        val businessLicense = binding.etBusinessLicense.text.toString().trim()

        val updatedProfile = Company(
            uid = currentUser.uid,
            companyName = companyName,
            phoneNumber = phoneNumber,
            email = email,
            address = address,
            industryType = companyType.ifEmpty { null },
            businessLicense = businessLicense.ifEmpty { null }
        )

        if (selectedImageUri != null) {
            // Upload image first, then update profile
            binding.btnSaveChanges.isEnabled = false
            binding.btnSaveChanges.text = "Uploading image..."

            cloudinaryHelper.uploadImage(selectedImageUri!!, requireContext()) { imageUrl ->
                activity?.runOnUiThread {
                    if (imageUrl != null) {
                        updatedProfile.profileImageUrl = imageUrl
                        viewModel.updateCompanyProfile(updatedProfile)
                    } else {
                        Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                        binding.btnSaveChanges.isEnabled = true
                        binding.btnSaveChanges.text = "Save Changes"
                    }
                }
            }
        } else {
            viewModel.updateCompanyProfile(updatedProfile)
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true

        // Company name validation
        if (binding.etCompanyName.text.toString().trim().isEmpty()) {
            binding.etCompanyName.error = "Company name is required"
            isValid = false
        }

        // Phone number validation
        val phoneNumber = binding.etPhoneNumber.text.toString().trim()
        if (phoneNumber.isEmpty()) {
            binding.etPhoneNumber.error = "Phone number is required"
            isValid = false
        } else if (phoneNumber.length < 10) {
            binding.etPhoneNumber.error = "Please enter a valid phone number"
            isValid = false
        }

        // Email validation
        val email = binding.etEmail.text.toString().trim()
        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Please enter a valid email address"
            isValid = false
        }

        // Address validation
        if (binding.etAddress.text.toString().trim().isEmpty()) {
            binding.etAddress.error = "Address is required"
            isValid = false
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}