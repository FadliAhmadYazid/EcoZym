package com.ecozym.wastemanagement.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class ProgressTracking(
    @DocumentId
    val id: String = "",
    val companyId: String = "",
    val companyName: String = "",
    val wasteType: String = "",
    val transportId: String = "",
    val quantity: Double = 0.0,
    val status: String = "", // "Scheduled", "In Transit", "Processing", "Returning", "Delivered"
    val driverName: String = "",
    val vehicleType: String = "",
    val licensePlate: String = "",
    val pickupDate: Timestamp? = null,
    val estimatedArrival: Timestamp? = null,
    val estimatedReturn: Timestamp? = null,
    val actualPickupTime: Timestamp? = null,
    val actualDeliveryTime: Timestamp? = null,
    val notes: String? = null,
    val wasteRegistrationId: String? = null, // Link to original waste registration
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)