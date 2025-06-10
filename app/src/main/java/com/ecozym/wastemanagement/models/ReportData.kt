package com.ecozym.wastemanagement.models

import com.google.firebase.Timestamp

data class ReportData(
    val companyId: String = "",
    val month: Int = 0,
    val year: Int = 0,
    val wasteType: String = "",
    val totalWasteSubmitted: Double = 0.0,
    val totalWasteProcessed: Double = 0.0,
    val totalEnzymeProduced: Double = 0.0,
    val co2Offset: Double = 0.0,
    val numberOfPickups: Int = 0,
    val averageProcessingTime: Double = 0.0,
    val costSavings: Double = 0.0,
    val wasteBreakdown: Map<String, Double> = emptyMap(),
    val monthlyTrend: List<MonthlyData> = emptyList(),
    val environmentalImpact: EnvironmentalMetrics = EnvironmentalMetrics(),
    val generatedAt: Timestamp = Timestamp.now()
)

data class MonthlyData(
    val month: String = "",
    val wasteAmount: Double = 0.0,
    val enzymeProduced: Double = 0.0
)

data class EnvironmentalMetrics(
    val treesEquivalent: Int = 0,
    val waterSaved: Double = 0.0,
    val energySaved: Double = 0.0,
    val carbonFootprintReduction: Double = 0.0
)