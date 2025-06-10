package com.ecozym.wastemanagement.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecozym.wastemanagement.models.AdminReportData
import com.ecozym.wastemanagement.repositories.AdminReportRepository
import kotlinx.coroutines.launch

class AdminReportViewModel : ViewModel() {

    private val adminReportRepository = AdminReportRepository()

    private val _adminReportData = MutableLiveData<AdminReportData>()
    val adminReportData: LiveData<AdminReportData> = _adminReportData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadAdminReport(month: Int, year: Int, wasteType: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val reportData = adminReportRepository.getAdminReportData(month, year, wasteType)
                _adminReportData.value = reportData
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load report data"
                // Provide default empty data
                _adminReportData.value = AdminReportData(
                    totalWaste = 0.0,
                    totalEnzyme = 0.0,
                    avgProcessingTime = 0.0,
                    totalCompanies = 0,
                    monthlyGrowth = 0.0,
                    avgLoadSize = 0.0,
                    topContributors = emptyList()
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshData(month: Int, year: Int, wasteType: String) {
        loadAdminReport(month, year, wasteType)
    }
}