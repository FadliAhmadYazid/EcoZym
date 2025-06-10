package com.ecozym.wastemanagement.fragments.company

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ecozym.wastemanagement.databinding.FragmentRegisterWasteBinding
import com.ecozym.wastemanagement.models.WasteRegistration
import com.ecozym.wastemanagement.utils.LocationHelper
import com.ecozym.wastemanagement.viewmodels.WasteViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@AndroidEntryPoint
class RegisterWasteFragment : Fragment() {

    private var _binding: FragmentRegisterWasteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WasteViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationHelper: LocationHelper

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterWasteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationHelper = LocationHelper(requireContext())

        setupSpinner()
        setupClickListeners()
        setupObservers()
        updatePriceBasedOnWasteType()
    }

    private fun setupSpinner() {
        val wasteTypes = arrayOf("BioPeel", "CitrusCycle", "FermaFruit")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, wasteTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerWasteType.adapter = adapter

        binding.spinnerWasteType.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                updatePriceBasedOnWasteType()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })
    }

    private fun setupClickListeners() {
        binding.btnCurrentLocation.setOnClickListener {
            getCurrentLocation()
        }

        binding.btnSubmit.setOnClickListener {
            submitWasteRegistration()
        }

        binding.tvPricingGuide.setOnClickListener {
            // Navigate to guide fragment
            parentFragmentManager.beginTransaction()
                .replace(com.ecozym.wastemanagement.R.id.fragmentContainer, GuideFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnSubmit.isEnabled = !isLoading
            binding.btnSubmit.text = if (isLoading) "Submitting..." else "Submit Waste Registration"
        }

        viewModel.registrationResult.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = {
                    Toast.makeText(context, "Waste registration submitted successfully. Waiting for admin approval.", Toast.LENGTH_LONG).show()
                    clearForm()
                },
                onFailure = { error ->
                    Toast.makeText(context, "Failed to submit: ${error.message}", Toast.LENGTH_SHORT).show()
                },
                onLoading = {
                    // Handle loading state
                }
            )
        }
    }

    private fun updatePriceBasedOnWasteType() {
        val selectedWasteType = binding.spinnerWasteType.selectedItem.toString()
        val price = viewModel.getWastePrice(selectedWasteType)
        binding.etPrice.setText(price.toInt().toString())
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        locationHelper.getCurrentLocation { location ->
            if (location != null) {
                getAddressFromLocation(location.latitude, location.longitude) { address ->
                    binding.etLocation.setText(address)
                }
            } else {
                Toast.makeText(context, "Failed to get current location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double, callback: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)

                val address = if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    "${addr.getAddressLine(0) ?: ""}, ${addr.locality ?: ""}, ${addr.adminArea ?: ""}"
                } else {
                    "Location: $latitude, $longitude"
                }

                withContext(Dispatchers.Main) {
                    callback(address)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback("Location: $latitude, $longitude")
                }
            }
        }
    }

    private fun submitWasteRegistration() {
        val wasteType = binding.spinnerWasteType.selectedItem.toString()
        val quantity = binding.etWasteQuantity.text.toString().trim()
        val price = binding.etPrice.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()

        if (quantity.isEmpty()) {
            binding.etWasteQuantity.error = "Quantity is required"
            return
        }

        if (quantity.toDoubleOrNull() == null || quantity.toDouble() < 5.0) {
            binding.etWasteQuantity.error = "Minimum quantity is 5 kg"
            return
        }

        if (price.isEmpty()) {
            binding.etPrice.error = "Price is required"
            return
        }

        if (location.isEmpty()) {
            binding.etLocation.error = "Location is required"
            return
        }

        val currentUser = auth.currentUser
        currentUser?.let { user ->
            // First get company name from user document
            getCompanyName(user.uid) { companyName ->
                val quantityValue = quantity.toDouble()
                val priceValue = price.toDouble()

                val wasteRegistration = WasteRegistration(
                    companyId = user.uid,
                    companyName = companyName,
                    wasteType = wasteType,
                    quantity = quantityValue,
                    pricePerKg = priceValue,
                    totalPrice = quantityValue * priceValue,
                    location = location,
                    pickupAddress = location,
                    status = "pending", // Always starts as pending
                    createdAt = com.google.firebase.Timestamp.now()
                )

                viewModel.registerWaste(wasteRegistration)
            }
        }
    }

    private fun getCompanyName(userId: String, callback: (String) -> Unit) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val companyName = document.getString("companyName")
                    ?: document.getString("name")
                    ?: "Unknown Company"
                callback(companyName)
            }
            .addOnFailureListener {
                callback("Unknown Company")
            }
    }

    private fun clearForm() {
        binding.etWasteQuantity.text?.clear()
        binding.etLocation.text?.clear()
        binding.spinnerWasteType.setSelection(0)
        updatePriceBasedOnWasteType()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}