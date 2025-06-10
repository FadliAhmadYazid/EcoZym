package com.ecozym.wastemanagement.models

data class AdminReportData(
    val totalWaste: Double = 0.0,
    val totalEnzyme: Double = 0.0,
    val avgProcessingTime: Double = 0.0,
    val totalCompanies: Int = 0,
    val monthlyGrowth: Double = 0.0,
    val avgLoadSize: Double = 0.0,
    val co2Offset: Double = 0.0,
    val topContributors: List<TopContributor> = emptyList(),
    val recentProgress: List<ProgressTracking>? = null,
    val recentProduction: List<Production>? = null,
    val recentActivities: List<Notification>? = null
)