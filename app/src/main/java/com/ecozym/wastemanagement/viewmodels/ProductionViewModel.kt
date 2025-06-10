package com.ecozym.wastemanagement.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecozym.wastemanagement.models.Production
import com.ecozym.wastemanagement.repositories.ProductionRepository
import com.ecozym.wastemanagement.utils.Result
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ProductionViewModel : ViewModel() {

    private val productionRepository = ProductionRepository()
    private val firestore = FirebaseFirestore.getInstance()

    private val _productionList = MutableLiveData<List<Production>>()
    val productionList: LiveData<List<Production>> = _productionList

    private val _updateResult = MutableLiveData<Result<Unit>>()
    val updateResult: LiveData<Result<Unit>> = _updateResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadProduction(status: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val productionList = if (status != null) {
                    productionRepository.getProductionByStatus(status)
                } else {
                    productionRepository.getAllProduction()
                }
                _productionList.value = productionList
            } catch (e: Exception) {
                _productionList.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadProductionPreview() {
        viewModelScope.launch {
            try {
                val productionList = productionRepository.getRecentProduction(3)
                _productionList.value = productionList
            } catch (e: Exception) {
                _productionList.value = emptyList()
            }
        }
    }

    fun updateProductionStatus(productionId: String, newStatus: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                productionRepository.updateProductionStatus(productionId, newStatus)
                _updateResult.value = Result.success(Unit)
                // Reload production list
                loadProduction()
            } catch (e: Exception) {
                _updateResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchProduction(query: String) {
        viewModelScope.launch {
            try {
                val productionList = productionRepository.searchProduction(query)
                _productionList.value = productionList
            } catch (e: Exception) {
                _productionList.value = emptyList()
            }
        }
    }

    fun updateProductionProgress(productionId: String, progress: Int) {
        viewModelScope.launch {
            try {
                productionRepository.updateProductionProgress(productionId, progress)
                _updateResult.value = Result.success(Unit)
            } catch (e: Exception) {
                _updateResult.value = Result.failure(e)
            }
        }
    }
}