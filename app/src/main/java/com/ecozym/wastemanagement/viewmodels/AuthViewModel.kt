package com.ecozym.wastemanagement.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecozym.wastemanagement.models.User
import com.ecozym.wastemanagement.repositories.AuthRepository
import com.ecozym.wastemanagement.utils.Result
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _loginResult = MutableLiveData<Result<User>>()
    val loginResult: LiveData<Result<User>> = _loginResult

    private val _registerResult = MutableLiveData<Result<String>>()
    val registerResult: LiveData<Result<String>> = _registerResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginResult.value = Result.Loading
            try {
                val result = authRepository.signInWithEmailAndPassword(email, password)
                _loginResult.value = result

                // If login successful, update current user
                if (result is Result.Success) {
                    _currentUser.value = result.data
                }
            } catch (e: Exception) {
                _loginResult.value = Result.Failure(e)
                _error.value = e.message ?: "Login failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(userData: Map<String, Any>) {
        viewModelScope.launch {
            _isLoading.value = true
            _registerResult.value = Result.Loading
            try {
                val result = authRepository.createUserWithEmailAndPassword(userData)
                _registerResult.value = result
            } catch (e: Exception) {
                _registerResult.value = Result.Failure(e)
                _error.value = e.message ?: "Registration failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getCurrentUser() {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                _currentUser.value = user
            } catch (e: Exception) {
                _currentUser.value = null
                _error.value = e.message ?: "Failed to get current user"
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _currentUser.value = null
                clearResults()
            } catch (e: Exception) {
                _error.value = e.message ?: "Logout failed"
            }
        }
    }

    fun checkUserRole(uid: String, callback: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val role = authRepository.getUserRole(uid)
                callback(role)
            } catch (e: Exception) {
                callback(null)
                _error.value = e.message ?: "Failed to check user role"
            }
        }
    }

    fun resetPassword(email: String): LiveData<Result<String>> {
        val result = MutableLiveData<Result<String>>()
        viewModelScope.launch {
            result.value = Result.Loading
            try {
                val resetResult = authRepository.resetPassword(email)
                result.value = resetResult
            } catch (e: Exception) {
                result.value = Result.Failure(e)
            }
        }
        return result
    }

    fun updateUserStatus(uid: String, status: String): LiveData<Result<Unit>> {
        val result = MutableLiveData<Result<Unit>>()
        viewModelScope.launch {
            result.value = Result.Loading
            try {
                val updateResult = authRepository.updateUserStatus(uid, status)
                result.value = updateResult
            } catch (e: Exception) {
                result.value = Result.Failure(e)
            }
        }
        return result
    }

    fun getAllUsers(): LiveData<Result<List<User>>> {
        val result = MutableLiveData<Result<List<User>>>()
        viewModelScope.launch {
            result.value = Result.Loading
            try {
                val usersResult = authRepository.getAllUsers()
                result.value = usersResult
            } catch (e: Exception) {
                result.value = Result.Failure(e)
            }
        }
        return result
    }

    fun getUsersByRole(role: String): LiveData<Result<List<User>>> {
        val result = MutableLiveData<Result<List<User>>>()
        viewModelScope.launch {
            result.value = Result.Loading
            try {
                val usersResult = authRepository.getUsersByRole(role)
                result.value = usersResult
            } catch (e: Exception) {
                result.value = Result.Failure(e)
            }
        }
        return result
    }

    fun getUsersByStatus(status: String): LiveData<Result<List<User>>> {
        val result = MutableLiveData<Result<List<User>>>()
        viewModelScope.launch {
            result.value = Result.Loading
            try {
                val usersResult = authRepository.getUsersByStatus(status)
                result.value = usersResult
            } catch (e: Exception) {
                result.value = Result.Failure(e)
            }
        }
        return result
    }

    fun getCurrentUserId(): String? {
        return authRepository.getCurrentUserId()
    }

    fun clearError() {
        _error.value = null
    }

    fun clearResults() {
        _loginResult.value = Result.Loading
        _registerResult.value = Result.Loading
    }

    // Helper function to check if user is logged in
    fun isUserLoggedIn(): Boolean {
        return authRepository.getCurrentUserId() != null
    }

    // Helper function to get current user role
    fun getCurrentUserRole(callback: (String?) -> Unit) {
        val currentUserId = getCurrentUserId()
        if (currentUserId != null) {
            checkUserRole(currentUserId, callback)
        } else {
            callback(null)
        }
    }
}