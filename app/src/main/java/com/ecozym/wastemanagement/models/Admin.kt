package com.ecozym.wastemanagement.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Admin(
    @DocumentId
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val adminRole: String = "", // "super_admin", "progress_admin", "production_admin"
    val phoneNumber: String? = null,
    val profileImageUrl: String? = null,
    val permissions: List<String> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)
