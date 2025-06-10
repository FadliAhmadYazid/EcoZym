// Production.kt
package com.ecozym.wastemanagement.models

import com.google.firebase.Timestamp

data class Production(
    val id: String = "",
    val batchId: String = "",
    val wasteType: String = "",
    val status: String = "",
    val sourceName: String = "",
    val inputQuantity: Double = 0.0,
    val outputQuantity: Double = 0.0,
    val estimatedCompletion: Timestamp? = null,
    val actualCompletion: Timestamp? = null,
    val progress: Int = 0,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val stage: String = "", // Changed from CharSequence? to String
    val companyId: String = "" // Added companyId field
)