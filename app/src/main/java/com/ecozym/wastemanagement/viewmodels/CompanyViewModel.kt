package com.ecozym.wastemanagement.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecozym.wastemanagement.models.*
import com.ecozym.wastemanagement.repositories.CompanyRepository
import com.ecozym.wastemanagement.utils.Result
import kotlinx.coroutines.launch

class CompanyViewModel : ViewModel() {

    private val companyRepository = CompanyRepository()

    private val _companyProfile = MutableLiveData<Company>()
    val companyProfile: LiveData<Company> = _companyProfile

    private val _totalWaste = MutableLiveData<Double>()
    val totalWaste: LiveData<Double> = _totalWaste

    private val _latestPickup = MutableLiveData<WasteTracking?>()
    val latestPickup: LiveData<WasteTracking?> = _latestPickup

    private val _activeProduction = MutableLiveData<Production?>()
    val activeProduction: LiveData<Production?> = _activeProduction

    private val _reportData = MutableLiveData<ReportData>()
    val reportData: LiveData<ReportData> = _reportData

    private val _updateResult = MutableLiveData<Result<Unit>>()
    val updateResult: LiveData<Result<Unit>> = _updateResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadCompanyProfile(companyId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val profile = companyRepository.getCompanyProfile(companyId)
                _companyProfile.value = profile
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadCompanyData(companyId: String) {
        viewModelScope.launch {
            try {
                // Load total waste
                val waste = companyRepository.getTotalWasteRegistered(companyId)
                _totalWaste.value = waste

                // Load latest pickup
                val pickup = companyRepository.getLatestPickup(companyId)
                _latestPickup.value = pickup

                // Load active production
                val production = companyRepository.getActiveProduction(companyId)
                _activeProduction.value = production
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun loadReport(
        companyId: String,
        month: Int,
        year: Int,
        wasteType: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val report = companyRepository.getCompanyReport(companyId, month, year, wasteType)
                _reportData.value = report
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCompanyProfile(company: Company) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                companyRepository.updateCompanyProfile(company)
                _updateResult.value = Result.success(Unit)
                // Reload profile
                loadCompanyProfile(company.uid)
            } catch (e: Exception) {
                _updateResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}