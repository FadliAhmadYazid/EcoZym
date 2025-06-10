package com.ecozym.wastemanagement.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecozym.wastemanagement.models.*
import com.ecozym.wastemanagement.repositories.ProgressRepository
import com.ecozym.wastemanagement.utils.Result
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ProgressViewModel : ViewModel() {

    private val progressRepository = ProgressRepository()
    private val firestore = FirebaseFirestore.getInstance()

    private val _progressList = MutableLiveData<List<ProgressTracking>>()
    val progressList: LiveData<List<ProgressTracking>> = _progressList

    private val _dashboardStats = MutableLiveData<DashboardStats>()
    val dashboardStats: LiveData<DashboardStats> = _dashboardStats

    private val _environmentalImpact = MutableLiveData<EnvironmentalImpact>()
    val environmentalImpact: LiveData<EnvironmentalImpact> = _environmentalImpact

    private val _updateResult = MutableLiveData<Result<Unit>>()
    val updateResult: LiveData<Result<Unit>> = _updateResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadProgress(status: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val progressList = if (status != null) {
                    progressRepository.getProgressByStatus(status)
                } else {
                    progressRepository.getAllProgress()
                }
                _progressList.value = progressList
            } catch (e: Exception) {
                _progressList.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadProgressPreview() {
        viewModelScope.launch {
            try {
                val progressList = progressRepository.getRecentProgress(3)
                _progressList.value = progressList
            } catch (e: Exception) {
                _progressList.value = emptyList()
            }
        }
    }

    fun loadDashboardStats() {
        viewModelScope.launch {
            try {
                val stats = progressRepository.getDashboardStats()
                _dashboardStats.value = stats
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun loadEnvironmentalImpact(month: Int, year: Int, wasteType: String) {
        viewModelScope.launch {
            try {
                val impact = progressRepository.getEnvironmentalImpact(month, year, wasteType)
                _environmentalImpact.value = impact
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateProgressStatus(progressId: String, newStatus: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                progressRepository.updateProgressStatus(progressId, newStatus)
                _updateResult.value = Result.success(Unit)
                // Reload progress list
                loadProgress()
            } catch (e: Exception) {
                _updateResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchProgress(query: String) {
        viewModelScope.launch {
            try {
                val progressList = progressRepository.searchProgress(query)
                _progressList.value = progressList
            } catch (e: Exception) {
                _progressList.value = emptyList()
            }
        }
    }
}