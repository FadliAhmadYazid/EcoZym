package com.ecozym.wastemanagement.models

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String? = null,
    val role: String = "", // "company", "admin", "driver", "super_admin"
    val status: String = "pending", // "pending", "approved", "rejected", "active", "inactive"
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),

    // Company specific fields
    val companyName: String? = null,
    val industryType: String? = null,
    val companySize: String? = null,
    val establishedYear: Int? = null,
    val website: String? = null,
    val licenseNumber: String? = null,

    // Admin specific fields
    val adminRole: String? = null, // "super_admin", "progress_admin", "production_admin"
    val permissions: List<String> = emptyList(),

    // Driver specific fields
    val driverLicense: String? = null,
    val vehicleType: String? = null,
    val licensePlate: String? = null,
    val experience: Int? = null,

    // Common contact fields
    val phone: String? = null,
    val address: String? = null,
    val city: String? = null,
    val postalCode: String? = null,
    val country: String? = null,

    // Document fields
    val documentUrl: String? = null,
    val profileImageUrl: String? = null,

    // Additional fields
    val isEmailVerified: Boolean = false,
    val lastLoginAt: Timestamp? = null,
    val notes: String? = null
)