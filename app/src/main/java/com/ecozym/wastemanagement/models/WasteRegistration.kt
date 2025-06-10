package com.ecozym.wastemanagement.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class WasteRegistration(
    @DocumentId
    val id: String = "",
    val companyId: String = "",
    val companyName: String = "", // Keep empty string as default, handle in UI
    val wasteType: String = "",
    val quantity: Double = 0.0,
    val pricePerKg: Double = 0.0,
    val totalPrice: Double = 0.0,
    val location: String = "",
    val pickupAddress: String = "",
    val coordinates: String? = null,
    val notes: String? = null,
    val status: String = "pending", // pending, approved, rejected, completed
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val scheduledPickupDate: Timestamp? = null,
    val completedAt: Timestamp? = null,
    val assignedDriverId: String? = null,
    val transportId: String? = null,
    val batchId: String? = null
) {
    // Helper function to get display name
    fun getDisplayCompanyName(): String {
        return if (companyName.isBlank()) {
            "Company ID: ${companyId.take(8)}..." // Show first 8 chars of company ID
        } else {
            companyName
        }
    }

    // Helper function to get formatted quantity
    fun getFormattedQuantity(): String {
        return "${quantity.toInt()} kg"
    }

    // Helper function to get formatted price
    fun getFormattedTotalPrice(): String {
        return "Rp ${String.format("%,d", totalPrice.toInt())}"
    }

    // Helper function to get status display
    fun getStatusDisplay(): String {
        return when (status.lowercase()) {
            "pending" -> "Pending"
            "approved" -> "Approved"
            "rejected" -> "Rejected"
            "completed" -> "Completed"
            else -> status.replaceFirstChar { it.uppercase() }
        }
    }

    // Helper function to check if data is valid
    fun isValid(): Boolean {
        return companyId.isNotBlank() &&
            wasteType.isNotBlank() &&
            quantity > 0 &&
            location.isNotBlank()
    }
}