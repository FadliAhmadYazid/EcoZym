package com.ecozym.wastemanagement.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Notification(
    @DocumentId
    val id: String = "",
    val userId: String = "", // "admin" for admin notifications, or specific user ID
    val type: String = "", // "admin", "company", "driver"
    val title: String = "",
    val message: String = "",
    val data: Map<String, String> = emptyMap(), // Additional data for actions
    val isRead: Boolean = false,
    val read: Boolean = false, // Tambahkan field ini untuk kompatibilitas Firestore
    val readAt: Timestamp? = null,
    val createdAt: Timestamp = Timestamp.now()
)