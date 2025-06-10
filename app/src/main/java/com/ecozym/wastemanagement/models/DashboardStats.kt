package com.ecozym.wastemanagement.models

data class DashboardStats(
    val totalWaste: Double = 0.0,
    val totalEnzyme: Double = 0.0,
    val totalCompanies: Int = 0,
    val activeTransports: Int = 0,
    val completedBatches: Int = 0
)