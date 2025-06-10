package com.ecozym.wastemanagement.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecozym.wastemanagement.models.User
import com.ecozym.wastemanagement.models.PartnerStats
import com.ecozym.wastemanagement.repositories.PartnerRepository
import com.ecozym.wastemanagement.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PartnerViewModel @Inject constructor(
    private val partnerRepository: PartnerRepository
) : ViewModel() {

    private val TAG = "PartnerViewModel"

    private val _partnerStats = MutableLiveData<PartnerStats>()
    val partnerStats: LiveData<PartnerStats> = _partnerStats

    private val _pendingPartners = MutableLiveData<List<User>>()
    val pendingPartners: LiveData<List<User>> = _pendingPartners

    private val _registeredPartners = MutableLiveData<List<User>>()
    val registeredPartners: LiveData<List<User>> = _registeredPartners

    private val _actionResult = MutableLiveData<Result<Unit>>()
    val actionResult: LiveData<Result<Unit>> = _actionResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadPartnerStats() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading partner stats...")
                val stats = partnerRepository.getPartnerStats()
                _partnerStats.value = stats
                Log.d(TAG, "Partner stats loaded successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading partner stats", e)
            }
        }
    }

    fun loadPendingPartners() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Loading pending partners...")
                val partners = partnerRepository.getPendingPartners()
                _pendingPartners.value = partners
                Log.d(TAG, "Pending partners loaded: ${partners.size} items")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading pending partners", e)
                _pendingPartners.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadRegisteredPartners(filterType: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Loading registered partners with filter: $filterType")
                val partners = partnerRepository.getRegisteredPartners(filterType)
                _registeredPartners.value = partners
                Log.d(TAG, "Registered partners loaded: ${partners.size} items")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading registered partners", e)
                _registeredPartners.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun approvePartner(partnerId: String) {
        viewModelScope.launch {
            _actionResult.value = Result.loading()
            try {
                Log.d(TAG, "Approving partner: $partnerId")
                partnerRepository.approvePartner(partnerId)
                _actionResult.value = Result.success(Unit)
                Log.d(TAG, "Partner approved successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error approving partner", e)
                _actionResult.value = Result.failure(e)
            }
        }
    }

    fun rejectPartner(partnerId: String) {
        viewModelScope.launch {
            _actionResult.value = Result.loading()
            try {
                Log.d(TAG, "Rejecting partner: $partnerId")
                partnerRepository.rejectPartner(partnerId)
                _actionResult.value = Result.success(Unit)
                Log.d(TAG, "Partner rejected successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error rejecting partner", e)
                _actionResult.value = Result.failure(e)
            }
        }
    }

    fun deletePartner(partnerId: String) {
        viewModelScope.launch {
            _actionResult.value = Result.loading()
            try {
                Log.d(TAG, "Deleting partner: $partnerId")
                partnerRepository.deletePartner(partnerId)
                _actionResult.value = Result.success(Unit)
                Log.d(TAG, "Partner deleted successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting partner", e)
                _actionResult.value = Result.failure(e)
            }
        }
    }

    fun searchPendingPartners(query: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Searching pending partners with query: '$query'")
                val partners = partnerRepository.searchPendingPartners(query)
                _pendingPartners.value = partners
                Log.d(TAG, "Pending partners search result: ${partners.size} items")
            } catch (e: Exception) {
                Log.e(TAG, "Error searching pending partners", e)
                _pendingPartners.value = emptyList()
            }
        }
    }

    fun searchRegisteredPartners(query: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Searching registered partners with query: '$query'")
                val partners = partnerRepository.searchRegisteredPartners(query)
                _registeredPartners.value = partners
                Log.d(TAG, "Registered partners search result: ${partners.size} items")
            } catch (e: Exception) {
                Log.e(TAG, "Error searching registered partners", e)
                _registeredPartners.value = emptyList()
            }
        }
    }

    fun addPartner(partnerData: Map<String, Any>) {
        viewModelScope.launch {
            _actionResult.value = Result.loading()
            try {
                Log.d(TAG, "Adding new partner")
                partnerRepository.addPartner(partnerData)
                _actionResult.value = Result.success(Unit)
                Log.d(TAG, "Partner added successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding partner", e)
                _actionResult.value = Result.failure(e)
            }
        }
    }

    fun updatePartner(partnerId: String, partnerData: Map<String, Any>) {
        viewModelScope.launch {
            _actionResult.value = Result.loading()
            try {
                Log.d(TAG, "Updating partner: $partnerId")
                partnerRepository.updatePartner(partnerId, partnerData)
                _actionResult.value = Result.success(Unit)
                Log.d(TAG, "Partner updated successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating partner", e)
                _actionResult.value = Result.failure(e)
            }
        }
    }
}