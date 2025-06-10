package com.ecozym.wastemanagement.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecozym.wastemanagement.models.WasteRegistration
import com.ecozym.wastemanagement.repositories.WasteRepository
import com.ecozym.wastemanagement.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WasteApprovalViewModel @Inject constructor(
    private val wasteRepository: WasteRepository
) : ViewModel() {

    private val TAG = "WasteApprovalViewModel"

    private val _pendingRegistrations = MutableStateFlow<List<WasteRegistration>>(emptyList())
    val pendingRegistrations: StateFlow<List<WasteRegistration>> = _pendingRegistrations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _approvalResult = MutableStateFlow<String?>(null)
    val approvalResult: StateFlow<String?> = _approvalResult.asStateFlow()

    init {
        loadPendingRegistrations()
    }

    fun loadPendingRegistrations() {
        Log.d(TAG, "🔄 Loading pending waste registrations...")

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            Log.d(TAG, "📡 Calling repository.getPendingWasteRegistrations()...")

            when (val result = wasteRepository.getPendingWasteRegistrations()) {
                is Result.Success -> {
                    Log.d(TAG, "✅ Repository returned success with ${result.data.size} items")

                    result.data.forEachIndexed { index, registration ->
                        Log.d(TAG, "📋 Registration $index:")
                        Log.d(TAG, "   ID: ${registration.id}")
                        Log.d(TAG, "   CompanyId: ${registration.companyId}")
                        Log.d(TAG, "   CompanyName: '${registration.companyName}'")
                        Log.d(TAG, "   WasteType: ${registration.wasteType}")
                        Log.d(TAG, "   Status: ${registration.status}")
                        Log.d(TAG, "   Quantity: ${registration.quantity}")
                        Log.d(TAG, "   Location: ${registration.location}")
                        Log.d(TAG, "   CreatedAt: ${registration.createdAt}")
                    }

                    _pendingRegistrations.value = result.data
                    Log.d(TAG, "🎯 StateFlow updated with ${result.data.size} registrations")
                }
                is Result.Failure -> {
                    Log.e(TAG, "❌ Repository returned failure", result.exception)
                    _error.value = result.exception.message ?: "Unknown error occurred"
                    _pendingRegistrations.value = emptyList()
                }

                Result.Loading -> TODO()
            }

            _isLoading.value = false
            Log.d(TAG, "✅ Loading completed. Final state:")
            Log.d(TAG, "   isLoading: ${_isLoading.value}")
            Log.d(TAG, "   pendingRegistrations.size: ${_pendingRegistrations.value.size}")
            Log.d(TAG, "   error: ${_error.value}")
        }
    }

    fun approveRegistration(registrationId: String, adminId: String) {
        Log.d(TAG, "👍 Approving registration: $registrationId")

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = wasteRepository.approveWasteRegistration(registrationId, adminId)) {
                is Result.Success -> {
                    Log.d(TAG, "✅ Registration approved successfully")
                    _approvalResult.value = "Registration approved successfully"
                    loadPendingRegistrations() // Reload the list
                }
                is Result.Failure -> {
                    Log.e(TAG, "❌ Failed to approve registration", result.exception)
                    _error.value = result.exception.message ?: "Failed to approve registration"
                }

                Result.Loading -> TODO()
            }

            _isLoading.value = false
        }
    }

    fun rejectRegistration(registrationId: String, adminId: String, reason: String? = null) {
        Log.d(TAG, "👎 Rejecting registration: $registrationId, reason: $reason")

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = wasteRepository.rejectWasteRegistration(registrationId, adminId, reason)) {
                is Result.Success -> {
                    Log.d(TAG, "✅ Registration rejected successfully")
                    _approvalResult.value = "Registration rejected successfully"
                    loadPendingRegistrations() // Reload the list
                }
                is Result.Failure -> {
                    Log.e(TAG, "❌ Failed to reject registration", result.exception)
                    _error.value = result.exception.message ?: "Failed to reject registration"
                }

                Result.Loading -> TODO()
            }

            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearApprovalResult() {
        _approvalResult.value = null
    }

    // Debug method untuk testing
    fun debugCurrentState() {
        Log.d(TAG, "🔍 DEBUG CURRENT STATE:")
        Log.d(TAG, "   isLoading: ${_isLoading.value}")
        Log.d(TAG, "   pendingRegistrations.size: ${_pendingRegistrations.value.size}")
        Log.d(TAG, "   error: ${_error.value}")
        Log.d(TAG, "   approvalResult: ${_approvalResult.value}")

        _pendingRegistrations.value.forEachIndexed { index, reg ->
            Log.d(TAG, "   Registration $index: ${reg.wasteType} - ${reg.companyName}")
        }
    }
}