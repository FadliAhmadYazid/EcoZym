package com.ecozym.wastemanagement.models

data class TopContributor(
    val companyId: String = "",
    val companyName: String = "",
    val totalWaste: Double = 0.0,
    val enzymeProduced: Double = 0.0,
    val contributionScore: Double = 0.0
)