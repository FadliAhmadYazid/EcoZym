package com.ecozym.wastemanagement.models

import com.google.firebase.Timestamp

data class Company(
    val uid: String = "",
    val companyName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val city: String? = null,
    val postalCode: String? = null,
    val country: String? = null,
    val industryType: String? = null,
    val companySize: String? = null,
    val establishedYear: Int? = null,
    val website: String? = null,
    val businessLicense: String? = null,
    var profileImageUrl: String? = null,
    val isVerified: Boolean = false,
    val status: String = "active", // active, inactive, suspended
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),

    // Additional company-specific fields
    val contactPersonName: String? = null,
    val contactPersonRole: String? = null,
    val alternatePhone: String? = null,
    val emergencyContact: String? = null,
    val taxId: String? = null,
    val bankAccount: String? = null,
    val notes: String? = null
)