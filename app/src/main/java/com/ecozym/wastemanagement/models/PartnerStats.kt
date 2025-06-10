package com.ecozym.wastemanagement.models

data class PartnerStats(
    val pendingCount: Int = 0,
    val registeredCount: Int = 0,
    val adminCount: Int = 0,
    val companyCount: Int = 0,
    val driverCount: Int = 0,
    val totalCount: Int = 0,
    val approvedThisMonth: Int = 0,
    val rejectedThisMonth: Int = 0
)