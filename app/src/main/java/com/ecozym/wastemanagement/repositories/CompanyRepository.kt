// CompanyRepository.kt
package com.ecozym.wastemanagement.repositories

import com.ecozym.wastemanagement.models.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.*

class CompanyRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val companiesCollection = firestore.collection("companies")
    private val wasteTrackingCollection = firestore.collection("waste_tracking")
    private val productionCollection = firestore.collection("production")

    // Flag untuk menggunakan dummy data (set true untuk testing)
    private val useDummyData = true

    suspend fun getCompanyProfile(companyId: String): Company {
        return if (useDummyData) {
            createDummyCompany(companyId)
        } else {
            try {
                val snapshot = companiesCollection.document(companyId).get().await()
                snapshot.toObject(Company::class.java) ?: createDummyCompany(companyId)
            } catch (e: Exception) {
                createDummyCompany(companyId)
            }
        }
    }

    suspend fun getTotalWasteRegistered(companyId: String): Double {
        return if (useDummyData) {
            3275.0 // Dummy data sesuai XML
        } else {
            try {
                val snapshot = wasteTrackingCollection
                    .whereEqualTo("companyId", companyId)
                    .get()
                    .await()

                val wasteRecords = snapshot.toObjects(WasteTracking::class.java)
                val total = wasteRecords.sumOf { it.quantity }

                // Fallback ke dummy data jika tidak ada data
                if (total == 0.0) 3275.0 else total
            } catch (e: Exception) {
                3275.0 // Fallback ke dummy data
            }
        }
    }

    suspend fun getLatestPickup(companyId: String): WasteTracking? {
        return if (useDummyData) {
            createDummyWasteTracking(companyId)
        } else {
            try {
                val snapshot = wasteTrackingCollection
                    .whereEqualTo("companyId", companyId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .await()

                val pickup = snapshot.toObjects(WasteTracking::class.java).firstOrNull()
                pickup ?: createDummyWasteTracking(companyId) // Fallback ke dummy
            } catch (e: Exception) {
                createDummyWasteTracking(companyId) // Fallback ke dummy
            }
        }
    }

    suspend fun getActiveProduction(companyId: String): Production? {
        return if (useDummyData) {
            createDummyProduction(companyId)
        } else {
            try {
                val snapshot = productionCollection
                    .whereEqualTo("companyId", companyId)
                    .whereNotEqualTo("status", "Completed")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .await()

                val production = snapshot.toObjects(Production::class.java).firstOrNull()
                production ?: createDummyProduction(companyId) // Fallback ke dummy
            } catch (e: Exception) {
                createDummyProduction(companyId) // Fallback ke dummy
            }
        }
    }

    suspend fun updateCompanyProfile(company: Company) {
        try {
            val updateData = company.copy(
                updatedAt = Timestamp.now()
            )
            companiesCollection.document(company.uid).set(updateData).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getCompanyReport(
        companyId: String,
        month: Int,
        year: Int,
        wasteType: String
    ): ReportData {
        return if (useDummyData) {
            createDummyReportData(companyId, month, year, wasteType)
        } else {
            try {
                // Firebase implementation (existing code)
                val calendar = Calendar.getInstance()
                calendar.set(year, month, 1, 0, 0, 0)
                val startOfMonth = Timestamp(calendar.time)

                calendar.add(Calendar.MONTH, 1)
                val startOfNextMonth = Timestamp(calendar.time)

                var wasteQuery = wasteTrackingCollection
                    .whereEqualTo("companyId", companyId)
                    .whereGreaterThanOrEqualTo("createdAt", startOfMonth)
                    .whereLessThan("createdAt", startOfNextMonth)

                if (wasteType != "All") {
                    wasteQuery = wasteQuery.whereEqualTo("wasteType", wasteType)
                }

                val wasteSnapshot = wasteQuery.get().await()
                val wasteRecords = wasteSnapshot.toObjects(WasteTracking::class.java)

                var productionQuery = productionCollection
                    .whereEqualTo("companyId", companyId)
                    .whereGreaterThanOrEqualTo("createdAt", startOfMonth)
                    .whereLessThan("createdAt", startOfNextMonth)

                if (wasteType != "All") {
                    productionQuery = productionQuery.whereEqualTo("wasteType", wasteType)
                }

                val productionSnapshot = productionQuery.get().await()
                val productionRecords = productionSnapshot.toObjects(Production::class.java)

                val totalWasteSubmitted = wasteRecords.sumOf { it.quantity }
                val totalWasteProcessed = productionRecords.sumOf { it.inputQuantity }
                val totalEnzymeProduced = productionRecords.sumOf { it.outputQuantity }
                val co2Offset = totalWasteProcessed * 0.5

                val wasteBreakdown = wasteRecords.groupBy { it.wasteType }
                    .mapValues { (_, records) -> records.sumOf { it.quantity } }

                val monthlyTrend = generateMonthlyTrend(companyId, wasteType)

                val environmentalImpact = EnvironmentalMetrics(
                    treesEquivalent = (co2Offset / 22).toInt(),
                    waterSaved = totalWasteProcessed * 1.5,
                    energySaved = totalWasteProcessed * 0.8,
                    carbonFootprintReduction = co2Offset
                )

                ReportData(
                    companyId = companyId,
                    month = month,
                    year = year,
                    wasteType = wasteType,
                    totalWasteSubmitted = totalWasteSubmitted,
                    totalWasteProcessed = totalWasteProcessed,
                    totalEnzymeProduced = totalEnzymeProduced,
                    co2Offset = co2Offset,
                    numberOfPickups = wasteRecords.size,
                    averageProcessingTime = calculateAverageProcessingTime(productionRecords),
                    costSavings = totalEnzymeProduced * 15.0,
                    wasteBreakdown = wasteBreakdown,
                    monthlyTrend = monthlyTrend,
                    environmentalImpact = environmentalImpact
                )
            } catch (e: Exception) {
                createDummyReportData(companyId, month, year, wasteType)
            }
        }
    }

    // DUMMY DATA CREATION METHODS
    private fun createDummyCompany(companyId: String): Company {
        return Company(
            uid = companyId,
            companyName = "Citrus Fresh Co.",
            email = "info@citrusfresh.com",
            phoneNumber = "+6281234567890",
            address = "Jl. Raya Industri No. 123",
            city = "Banda Aceh",
            postalCode = "23111",
            country = "Indonesia",
            industryType = "Food & Beverage",
            companySize = "Medium (50-200 employees)",
            establishedYear = 2018,
            website = "www.citrusfresh.com",
            isVerified = true,
            status = "active",
            contactPersonName = "Ahmad Rahman",
            contactPersonRole = "Environmental Manager",
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )
    }

    private fun createDummyWasteTracking(companyId: String): WasteTracking {
        // Create dummy date for March 31, 2025
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.MARCH, 31)
        val estimatedArrival = Timestamp(calendar.time)

        return WasteTracking(
            id = "dummy_tracking_001",
            companyId = companyId,
            companyName = "Citrus Fresh Co.",
            wasteType = "Organic Waste",
            quantity = 500.0,
            unit = "kg",
            status = "In Transit",
            driverName = "Budi Santoso",
            driverPhone = "+6281234567891",
            vehicleInfo = "Truck - B 1234 ABC",
            estimatedArrival = estimatedArrival,
            pickupAddress = "Jl. Raya Industri No. 123, Banda Aceh",
            vehicleType = "Truck",
            licensePlate = "B 1234 ABC",
            location = "Banda Aceh",
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )
    }

    private fun createDummyProduction(companyId: String): Production {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 15) // 15 days from now
        val estimatedCompletion = Timestamp(calendar.time)

        return Production(
            id = "dummy_production_001",
            batchId = "#WE-2024-003",
            wasteType = "Organic Waste",
            status = "In Progress",
            sourceName = "Citrus Fresh Co.",
            inputQuantity = 1000.0,
            outputQuantity = 650.0,
            estimatedCompletion = estimatedCompletion,
            progress = 65,
            stage = "Fermentation Stage",
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )
    }

    private fun createDummyReportData(
        companyId: String,
        month: Int,
        year: Int,
        wasteType: String
    ): ReportData {
        val monthlyTrend = listOf(
            MonthlyData("Oct", 2800.0, 1820.0),
            MonthlyData("Nov", 3100.0, 2015.0),
            MonthlyData("Dec", 2950.0, 1917.5),
            MonthlyData("Jan", 3275.0, 2128.75),
            MonthlyData("Feb", 3400.0, 2210.0),
            MonthlyData("Mar", 3600.0, 2340.0)
        )

        val wasteBreakdown = mapOf(
            "Organic Waste" to 2000.0,
            "Food Waste" to 800.0,
            "Agricultural Waste" to 475.0
        )

        val environmentalImpact = EnvironmentalMetrics(
            treesEquivalent = 75,
            waterSaved = 4912.5,
            energySaved = 2620.0,
            carbonFootprintReduction = 1637.5
        )

        return ReportData(
            companyId = companyId,
            month = month,
            year = year,
            wasteType = wasteType,
            totalWasteSubmitted = 3275.0,
            totalWasteProcessed = 3100.0,
            totalEnzymeProduced = 2015.0,
            co2Offset = 1637.5,
            numberOfPickups = 12,
            averageProcessingTime = 21.5,
            costSavings = 30225.0,
            wasteBreakdown = wasteBreakdown,
            monthlyTrend = monthlyTrend,
            environmentalImpact = environmentalImpact,
            generatedAt = Timestamp.now()
        )
    }

    private suspend fun generateMonthlyTrend(companyId: String, wasteType: String): List<MonthlyData> {
        // Return dummy trend if using dummy data
        if (useDummyData) {
            return listOf(
                MonthlyData("Oct", 2800.0, 1820.0),
                MonthlyData("Nov", 3100.0, 2015.0),
                MonthlyData("Dec", 2950.0, 1917.5),
                MonthlyData("Jan", 3275.0, 2128.75),
                MonthlyData("Feb", 3400.0, 2210.0),
                MonthlyData("Mar", 3600.0, 2340.0)
            )
        }

        // Original Firebase implementation
        return try {
            val trends = mutableListOf<MonthlyData>()
            val calendar = Calendar.getInstance()

            repeat(6) { i ->
                calendar.add(Calendar.MONTH, -1)
                val monthStart = Timestamp(calendar.time)

                calendar.add(Calendar.MONTH, 1)
                val monthEnd = Timestamp(calendar.time)

                var wasteQuery = wasteTrackingCollection
                    .whereEqualTo("companyId", companyId)
                    .whereGreaterThanOrEqualTo("createdAt", monthStart)
                    .whereLessThan("createdAt", monthEnd)

                if (wasteType != "All") {
                    wasteQuery = wasteQuery.whereEqualTo("wasteType", wasteType)
                }

                val wasteSnapshot = wasteQuery.get().await()
                val wasteAmount = wasteSnapshot.toObjects(WasteTracking::class.java).sumOf { it.quantity }

                var productionQuery = productionCollection
                    .whereEqualTo("companyId", companyId)
                    .whereGreaterThanOrEqualTo("createdAt", monthStart)
                    .whereLessThan("createdAt", monthEnd)

                if (wasteType != "All") {
                    productionQuery = productionQuery.whereEqualTo("wasteType", wasteType)
                }

                val productionSnapshot = productionQuery.get().await()
                val enzymeProduced = productionSnapshot.toObjects(Production::class.java).sumOf { it.outputQuantity }

                val monthName = java.text.SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)
                trends.add(MonthlyData(monthName, wasteAmount, enzymeProduced))

                calendar.add(Calendar.MONTH, -1)
            }

            trends.reversed()
        } catch (e: Exception) {
            // Return dummy data as fallback
            listOf(
                MonthlyData("Oct", 2800.0, 1820.0),
                MonthlyData("Nov", 3100.0, 2015.0),
                MonthlyData("Dec", 2950.0, 1917.5),
                MonthlyData("Jan", 3275.0, 2128.75),
                MonthlyData("Feb", 3400.0, 2210.0),
                MonthlyData("Mar", 3600.0, 2340.0)
            )
        }
    }

    private fun calculateAverageProcessingTime(productionRecords: List<Production>): Double {
        if (productionRecords.isEmpty()) return 21.5 // Dummy fallback

        val completedRecords = productionRecords.filter {
            it.actualCompletion != null && it.createdAt != null
        }

        if (completedRecords.isEmpty()) return 21.5 // Dummy fallback

        val totalTime = completedRecords.sumOf { production ->
            val start = production.createdAt.seconds
            val end = production.actualCompletion!!.seconds
            (end - start) / (24 * 60 * 60)
        }

        return totalTime.toDouble() / completedRecords.size
    }

    // Method untuk mengubah mode dummy data (untuk testing)
    fun setUseDummyData(useDummy: Boolean) {
        // Ini bisa diubah sesuai kebutuhan development/production
    }
}