package com.ecozym.wastemanagement.models

import com.google.firebase.Timestamp

data class WasteTracking(
    val id: String = "",
    val companyId: String = "",
    val companyName: String = "",
    val wasteType: String = "",
    val quantity: Double = 0.0,
    val unit: String = "kg",
    val pickupDate: Timestamp? = null,
    val scheduledDate: Timestamp? = null,
    val status: String = "pending", // pending, scheduled, picked_up, processing, completed
    val driverName: String? = null,
    val driverPhone: String? = null, // Added driver phone number
    val vehicleInfo: String? = null,
    val notes: String? = null,
    val location: String? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),

    // Additional properties needed by the adapter
    val isCompleted: Boolean = false, // Determines if tracking is completed
    val pickupAddress: String = "", // Address for pickup
    val estimatedArrival: Timestamp? = null, // Estimated arrival time
    val vehicleType: String? = null, // Type of vehicle (truck, van, etc.)
    val licensePlate: String? = null, // Vehicle license plate
    val completedAt: Timestamp? = null, // When the tracking was completed
    val batchId: String? = null // Batch identifier for completed items
)