package com.ecozym.wastemanagement.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecozym.wastemanagement.models.Admin
import com.ecozym.wastemanagement.repositories.AdminRepository
import com.ecozym.wastemanagement.utils.Result
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {
    private val adminRepository = AdminRepository()

    private val _adminProfile = MutableLiveData<Admin>()
    val adminProfile: LiveData<Admin> = _adminProfile

    private val _updateResult = MutableLiveData<Result<Unit>>()
    val updateResult: LiveData<Result<Unit>> = _updateResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Fixed: Changed to MutableLiveData<String?> and LiveData<String?>
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadAdminProfile(adminId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val profile = adminRepository.getAdminProfile(adminId)
                _adminProfile.value = profile
                _error.value = null // Clear previous errors
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load admin profile"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAdminProfile(admin: Admin) {
        viewModelScope.launch {
            _isLoading.value = true
            _updateResult.value = Result.Loading
            try {
                adminRepository.updateAdminProfile(admin)
                _updateResult.value = Result.Success(Unit)
                _error.value = null // Clear previous errors
                // Reload profile
                loadAdminProfile(admin.uid)
            } catch (e: Exception) {
                _updateResult.value = Result.Failure(e)
                _error.value = e.message ?: "Failed to update admin profile"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkAdminPermissions(adminId: String, permission: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val hasPermission = adminRepository.checkPermission(adminId, permission)
                callback(hasPermission)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to check permissions"
                callback(false)
            }
        }
    }

    fun createAdmin(admin: Admin) {
        viewModelScope.launch {
            _isLoading.value = true
            _updateResult.value = Result.Loading
            try {
                adminRepository.createAdmin(admin)
                _updateResult.value = Result.Success(Unit)
                _error.value = null // Clear previous errors
            } catch (e: Exception) {
                _updateResult.value = Result.Failure(e)
                _error.value = e.message ?: "Failed to create admin"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getAllAdmins(): LiveData<Result<List<Admin>>> {
        val result = MutableLiveData<Result<List<Admin>>>()
        viewModelScope.launch {
            result.value = Result.Loading
            try {
                val admins = adminRepository.getAllAdmins()
                result.value = Result.Success(admins)
            } catch (e: Exception) {
                result.value = Result.Failure(e)
                _error.value = e.message ?: "Failed to fetch admins"
            }
        }
        return result
    }

    fun deactivateAdmin(adminId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _updateResult.value = Result.Loading
            try {
                adminRepository.deactivateAdmin(adminId)
                _updateResult.value = Result.Success(Unit)
                _error.value = null // Clear previous errors
            } catch (e: Exception) {
                _updateResult.value = Result.Failure(e)
                _error.value = e.message ?: "Failed to deactivate admin"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearUpdateResult() {
        _updateResult.value = Result.Loading
    }
}