package com.ecozym.wastemanagement.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecozym.wastemanagement.models.WasteRegistration
import com.ecozym.wastemanagement.models.WasteTracking
import com.ecozym.wastemanagement.models.ProgressTracking
import com.ecozym.wastemanagement.repositories.WasteRepository
import com.ecozym.wastemanagement.utils.Result
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.*

@HiltViewModel
class WasteViewModel @Inject constructor(
    private val wasteRepository: WasteRepository
) : ViewModel() {

    private val _registrationResult = MutableLiveData<Result<String>>()
    val registrationResult: LiveData<Result<String>> = _registrationResult

    private val _trackingData = MutableLiveData<List<WasteTracking>>()
    val trackingData: LiveData<List<WasteTracking>> = _trackingData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun registerWaste(wasteRegistration: WasteRegistration) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = wasteRepository.registerWaste(wasteRegistration)
                _registrationResult.value = result
            } catch (e: Exception) {
                _registrationResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadOngoingTracking(companyId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            // Simulate loading delay
            delay(1000)

            // Always load dummy data for now
            _trackingData.value = createOngoingDummyData()
            _isLoading.value = false
        }
    }

    fun loadTrackingHistory(companyId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            // Simulate loading delay
            delay(1000)

            // Always load dummy data for now
            _trackingData.value = createHistoryDummyData()
            _isLoading.value = false
        }
    }

    private fun createOngoingDummyData(): List<WasteTracking> {
        val currentTime = Timestamp.now()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 2)
        val futureTime = Timestamp(calendar.time)

        return listOf(
            WasteTracking(
                id = "ongoing_1",
                companyId = "company_1",
                companyName = "PT Sumber Buah Indonesia",
                wasteType = "CitrusCycle",
                quantity = 200.0,
                unit = "kg",
                pickupDate = currentTime,
                scheduledDate = currentTime,
                status = "On Route",
                driverName = "BJ - Bang Jono",
                driverPhone = "+6281211349840",
                vehicleInfo = "Truck - B 4729 XYZ",
                notes = "Standard pickup for citrus waste",
                location = "45 Industri Raya St., Pulogadung Industrial Area, East Jakarta",
                createdAt = currentTime,
                updatedAt = currentTime,
                isCompleted = false,
                pickupAddress = "45 Industri Raya St., Pulogadung Industrial Area, East Jakarta",
                estimatedArrival = futureTime,
                vehicleType = "Truck",
                licensePlate = "B 4729 XYZ",
                completedAt = null,
                batchId = null
            ),
            WasteTracking(
                id = "ongoing_2",
                companyId = "company_2",
                companyName = "CV Fresh Produce",
                wasteType = "BioPeel",
                quantity = 150.0,
                unit = "kg",
                pickupDate = currentTime,
                scheduledDate = currentTime,
                status = "Scheduled",
                driverName = "AS - Andi Susanto",
                driverPhone = "+628987654321",
                vehicleInfo = "Van - B 1234 ABC",
                notes = "Organic waste pickup",
                location = "23 Fresh Market St., Tanah Abang, Central Jakarta",
                createdAt = currentTime,
                updatedAt = currentTime,
                isCompleted = false,
                pickupAddress = "23 Fresh Market St., Tanah Abang, Central Jakarta",
                estimatedArrival = futureTime,
                vehicleType = "Van",
                licensePlate = "B 1234 ABC",
                completedAt = null,
                batchId = null
            ),
            WasteTracking(
                id = "ongoing_3",
                companyId = "company_3",
                companyName = "Toko Buah Manis",
                wasteType = "FermaFruit",
                quantity = 175.0,
                unit = "kg",
                pickupDate = currentTime,
                scheduledDate = currentTime,
                status = "Pending",
                driverName = "RD - Ridwan",
                driverPhone = "+628111222333",
                vehicleInfo = "Truck - B 5555 GHI",
                notes = "Awaiting pickup schedule",
                location = "67 Fruit Center St., Kemayoran, Central Jakarta",
                createdAt = currentTime,
                updatedAt = currentTime,
                isCompleted = false,
                pickupAddress = "67 Fruit Center St., Kemayoran, Central Jakarta",
                estimatedArrival = futureTime,
                vehicleType = "Truck",
                licensePlate = "B 5555 GHI",
                completedAt = null,
                batchId = null
            )
        )
    }

    private fun createHistoryDummyData(): List<WasteTracking> {
        val pastTime1 = Timestamp(Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000)) // 7 days ago
        val pastTime2 = Timestamp(Date(System.currentTimeMillis() - 14 * 24 * 60 * 60 * 1000)) // 14 days ago
        val pastTime3 = Timestamp(Date(System.currentTimeMillis() - 21 * 24 * 60 * 60 * 1000)) // 21 days ago
        val pastTime4 = Timestamp(Date(System.currentTimeMillis() - 28 * 24 * 60 * 60 * 1000)) // 28 days ago

        return listOf(
            WasteTracking(
                id = "history_1",
                companyId = "company_1",
                companyName = "PT Sumber Buah Indonesia",
                wasteType = "FermaFruit",
                quantity = 180.0,
                unit = "kg",
                pickupDate = pastTime1,
                scheduledDate = pastTime1,
                status = "Completed",
                driverName = "BJ - Bang Jono",
                driverPhone = "+628123456789",
                vehicleInfo = "Truck - B 4729 XYZ",
                notes = "Successfully processed",
                location = "45 Industri Raya St., Pulogadung Industrial Area, East Jakarta",
                createdAt = pastTime1,
                updatedAt = pastTime1,
                isCompleted = true,
                pickupAddress = "45 Industri Raya St., Pulogadung Industrial Area, East Jakarta",
                estimatedArrival = null,
                vehicleType = "Truck",
                licensePlate = "B 4729 XYZ",
                completedAt = pastTime1,
                batchId = "FF-2025-029"
            ),
            WasteTracking(
                id = "history_2",
                companyId = "company_2",
                companyName = "CV Fresh Produce",
                wasteType = "CitrusCycle",
                quantity = 220.0,
                unit = "kg",
                pickupDate = pastTime2,
                scheduledDate = pastTime2,
                status = "Completed",
                driverName = "DW - Dedi Wijaya",
                driverPhone = "+628555666777",
                vehicleInfo = "Truck - B 8888 DEF",
                notes = "Processed into bio-fertilizer",
                location = "23 Fresh Market St., Tanah Abang, Central Jakarta",
                createdAt = pastTime2,
                updatedAt = pastTime2,
                isCompleted = true,
                pickupAddress = "23 Fresh Market St., Tanah Abang, Central Jakarta",
                estimatedArrival = null,
                vehicleType = "Truck",
                licensePlate = "B 8888 DEF",
                completedAt = pastTime2,
                batchId = "CC-2025-028"
            ),
            WasteTracking(
                id = "history_3",
                companyId = "company_3",
                companyName = "Toko Buah Segar",
                wasteType = "BioPeel",
                quantity = 95.0,
                unit = "kg",
                pickupDate = pastTime3,
                scheduledDate = pastTime3,
                status = "Completed",
                driverName = "AS - Andi Susanto",
                driverPhone = "+628987654321",
                vehicleInfo = "Van - B 1234 ABC",
                notes = "Small batch pickup",
                location = "12 Pasar Minggu St., South Jakarta",
                createdAt = pastTime3,
                updatedAt = pastTime3,
                isCompleted = true,
                pickupAddress = "12 Pasar Minggu St., South Jakarta",
                estimatedArrival = null,
                vehicleType = "Van",
                licensePlate = "B 1234 ABC",
                completedAt = pastTime3,
                batchId = "BP-2025-027"
            ),
            WasteTracking(
                id = "history_4",
                companyId = "company_4",
                companyName = "Pasar Buah Central",
                wasteType = "FermaFruit",
                quantity = 160.0,
                unit = "kg",
                pickupDate = pastTime4,
                scheduledDate = pastTime4,
                status = "Completed",
                driverName = "RD - Ridwan",
                driverPhone = "+628111222333",
                vehicleInfo = "Truck - B 5555 GHI",
                notes = "Monthly pickup completed",
                location = "89 Central Market St., Menteng, Central Jakarta",
                createdAt = pastTime4,
                updatedAt = pastTime4,
                isCompleted = true,
                pickupAddress = "89 Central Market St., Menteng, Central Jakarta",
                estimatedArrival = null,
                vehicleType = "Truck",
                licensePlate = "B 5555 GHI",
                completedAt = pastTime4,
                batchId = "FF-2025-026"
            )
        )
    }

    private fun convertProgressTrackingToWasteTracking(
        progressTracking: ProgressTracking,
        isCompleted: Boolean = false
    ): WasteTracking {
        return WasteTracking(
            id = progressTracking.id,
            companyId = "",
            companyName = progressTracking.companyName,
            wasteType = progressTracking.wasteType,
            quantity = progressTracking.quantity,
            status = progressTracking.status.lowercase(),
            driverName = progressTracking.driverName,
            vehicleInfo = "${progressTracking.vehicleType} - ${progressTracking.licensePlate}",
            pickupDate = progressTracking.pickupDate,
            scheduledDate = progressTracking.pickupDate,
            createdAt = progressTracking.createdAt,
            updatedAt = progressTracking.updatedAt,
            isCompleted = isCompleted,
            estimatedArrival = progressTracking.estimatedArrival,
            vehicleType = progressTracking.vehicleType,
            licensePlate = progressTracking.licensePlate,
            completedAt = if (isCompleted) progressTracking.updatedAt else null
        )
    }

    fun getWastePrice(wasteType: String): Double {
        return when (wasteType) {
            "BioPeel" -> 5500.0
            "CitrusCycle" -> 6000.0
            "FermaFruit" -> 4800.0
            else -> 0.0
        }
    }
}