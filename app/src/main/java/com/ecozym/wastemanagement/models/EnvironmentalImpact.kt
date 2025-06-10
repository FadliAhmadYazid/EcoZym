package com.ecozym.wastemanagement.models

data class EnvironmentalImpact(
    val totalWaste: Double = 0.0,
    val totalEnzyme: Double = 0.0,
    val co2Offset: Double = 0.0,
    val treesEquivalent: Int = 0,
    val waterSaved: Double = 0.0
)
