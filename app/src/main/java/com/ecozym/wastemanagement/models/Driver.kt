package com.ecozym.wastemanagement.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Driver(
    @DocumentId
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val vehicleType: String = "",
    val licensePlate: String = "",
    val profileImageUrl: String? = null,
    val isAvailable: Boolean = true,
    val currentLocation: String? = null,
    val totalTrips: Int = 0,
    val rating: Double = 0.0,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)
