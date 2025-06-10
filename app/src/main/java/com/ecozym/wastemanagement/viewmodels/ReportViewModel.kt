package com.ecozym.wastemanagement.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecozym.wastemanagement.models.AdminReportData
import com.ecozym.wastemanagement.repositories.AdminReportRepository
import kotlinx.coroutines.launch

class ReportViewModel : ViewModel() {
    private val adminReportRepository = AdminReportRepository()

    private val _adminReportData = MutableLiveData<AdminReportData>()
    val adminReportData: LiveData<AdminReportData> = _adminReportData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadAdminReport(month: Int, year: Int, wasteType: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val report = adminReportRepository.getAdminReportData(month, year, wasteType)
                _adminReportData.value = report
            } catch (e: Exception) {
                _error.value = "Failed to load report: ${e.message}"
                _adminReportData.value = AdminReportData() // Empty data as fallback
            } finally {
                _isLoading.value = false
            }
        }
    }

    // If you need export functionality, you'll need to add it to AdminReportRepository first
    fun exportReport(month: Int, year: Int, wasteType: String, callback: (String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // You'll need to implement exportReport method in AdminReportRepository
                // For now, this is a placeholder
                val reportData = adminReportRepository.getAdminReportData(month, year, wasteType)
                // Generate export URL or file path here
                val exportUrl = generateExportUrl(reportData, month, year, wasteType)
                callback(exportUrl)
            } catch (e: Exception) {
                _error.value = "Failed to export report: ${e.message}"
                callback(null)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun generateExportUrl(reportData: AdminReportData, month: Int, year: Int, wasteType: String): String {
        // Placeholder implementation - you would implement actual export logic here
        // This could involve creating a PDF, CSV, or uploading to cloud storage
        return "report_${month}${year}${wasteType}.pdf"
    }

    fun clearError() {
        _error.value = null
    }
}