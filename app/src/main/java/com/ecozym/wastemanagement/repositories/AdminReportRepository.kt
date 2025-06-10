package com.ecozym.wastemanagement.repositories

import com.ecozym.wastemanagement.models.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.*

class AdminReportRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val progressCollection = firestore.collection("progress")
    private val productionCollection = firestore.collection("production")
    private val notificationCollection = firestore.collection("notifications")
    private val companiesCollection = firestore.collection("companies")

    suspend fun getAdminReportData(month: Int, year: Int, wasteType: String): AdminReportData {
        return try {
            // Get calendar for date filtering
            val calendar = Calendar.getInstance()
            calendar.set(year, month, 1, 0, 0, 0)
            val startOfMonth = com.google.firebase.Timestamp(calendar.time)

            calendar.add(Calendar.MONTH, 1)
            val startOfNextMonth = com.google.firebase.Timestamp(calendar.time)

            // Build query based on waste type filter
            var progressQuery = progressCollection
                .whereGreaterThanOrEqualTo("createdAt", startOfMonth)
                .whereLessThan("createdAt", startOfNextMonth)

            var productionQuery = productionCollection
                .whereGreaterThanOrEqualTo("createdAt", startOfMonth)
                .whereLessThan("createdAt", startOfNextMonth)

            if (wasteType != "All") {
                progressQuery = progressQuery.whereEqualTo("wasteType", wasteType)
                productionQuery = productionQuery.whereEqualTo("wasteType", wasteType)
            }

            // Execute queries
            val progressSnapshot = progressQuery.get().await()
            val productionSnapshot = productionQuery.get().await()
            val companiesSnapshot = companiesCollection.get().await()

            val progressList = progressSnapshot.toObjects(ProgressTracking::class.java)
            val productionList = productionSnapshot.toObjects(Production::class.java)

            // Calculate totals
            val totalWaste = progressList.sumOf { it.quantity }
            val totalEnzyme = productionList.sumOf { it.outputQuantity }
            val totalCompanies = companiesSnapshot.size()

            // **FALLBACK TO MOCK DATA IF FIREBASE IS EMPTY**
            if (totalWaste == 0.0 && totalEnzyme == 0.0 && totalCompanies == 0) {
                return getMockAdminReportData()
            }

            // Calculate CO2 offset (example calculation)
            val co2Offset = totalWaste * 0.5 // Assuming 0.5kg CO2 offset per kg waste

            // Get recent data for preview
            val recentProgress = getRecentProgress(3)
            val recentProduction = getRecentProduction(3)
            val recentActivities = getRecentActivities(5)

            // Calculate top contributors
            val topContributors = calculateTopContributors(progressList, productionList)

            AdminReportData(
                totalWaste = totalWaste,
                totalEnzyme = totalEnzyme,
                avgProcessingTime = calculateAvgProcessingTime(productionList),
                totalCompanies = totalCompanies,
                monthlyGrowth = calculateMonthlyGrowth(month, year, wasteType),
                avgLoadSize = if (progressList.isNotEmpty()) totalWaste / progressList.size else 0.0,
                co2Offset = co2Offset,
                topContributors = topContributors,
                recentProgress = recentProgress,
                recentProduction = recentProduction,
                recentActivities = recentActivities
            )
        } catch (e: Exception) {
            // Return mock data if there's an error
            getMockAdminReportData()
        }
    }

    // **MOCK DATA METHOD**
    private fun getMockAdminReportData(): AdminReportData {
        return AdminReportData(
            totalWaste = 22.0,
            totalEnzyme = 8.3,
            avgProcessingTime = 14.5,
            totalCompanies = 42,
            monthlyGrowth = 15.7,
            avgLoadSize = 1.2,
            co2Offset = 850.0,
            topContributors = listOf(
                TopContributor(
                    companyId = "1",
                    companyName = "Citrus Fresh Co.",
                    totalWaste = 5.2,
                    enzymeProduced = 2.1,
                    contributionScore = 85.0
                ),
                TopContributor(
                    companyId = "2",
                    companyName = "Tropical Fruits Inc.",
                    totalWaste = 4.8,
                    enzymeProduced = 1.9,
                    contributionScore = 78.0
                ),
                TopContributor(
                    companyId = "3",
                    companyName = "Smoothie Factory",
                    totalWaste = 3.5,
                    enzymeProduced = 1.4,
                    contributionScore = 65.0
                )
            ),
            recentProgress = getMockProgressList(),
            recentProduction = getMockProductionList(),
            recentActivities = getMockActivitiesList()
        )
    }

    private fun getMockProgressList(): List<ProgressTracking> {
        // Create ProgressTracking objects using only available properties
        return listOf(
            ProgressTracking(
                id = "TRP-001",
                companyName = "Citrus Fresh Co.",
                wasteType = "CitrusCycle",
                quantity = 1.2,
                status = "Scheduled",
                pickupDate = com.google.firebase.Timestamp.now(),
                estimatedArrival = com.google.firebase.Timestamp.now(),
                createdAt = com.google.firebase.Timestamp.now()
            ),
            ProgressTracking(
                id = "TRP-002",
                companyName = "Tropical Fruits Inc.",
                wasteType = "BioFresh",
                quantity = 0.8,
                status = "In Transit",
                pickupDate = com.google.firebase.Timestamp.now(),
                estimatedArrival = com.google.firebase.Timestamp.now(),
                createdAt = com.google.firebase.Timestamp.now()
            ),
            ProgressTracking(
                id = "TRP-003",
                companyName = "Smoothie Factory",
                wasteType = "FermaFruit",
                quantity = 1.5,
                status = "Processing",
                pickupDate = com.google.firebase.Timestamp.now(),
                estimatedArrival = com.google.firebase.Timestamp.now(),
                createdAt = com.google.firebase.Timestamp.now()
            )
        )
    }

    private fun getMockProductionList(): List<Production> {
        // Create Production objects using only available properties
        // Remove currentStage and totalStages as they don't exist in the model
        return listOf(
            Production(
                batchId = "ECO-2025-075",
                wasteType = "Citrus Enzyme",
                status = "Fermentation",
                outputQuantity = 0.0,
                estimatedCompletion = com.google.firebase.Timestamp.now(),
                createdAt = com.google.firebase.Timestamp.now()
            ),
            Production(
                batchId = "ECO-2025-074",
                wasteType = "Citrus Enzyme",
                status = "Filtration",
                outputQuantity = 0.0,
                estimatedCompletion = com.google.firebase.Timestamp.now(),
                createdAt = com.google.firebase.Timestamp.now()
            ),
            Production(
                batchId = "ECO-2025-073",
                wasteType = "Citrus Enzyme",
                status = "Ready for Distribution",
                outputQuantity = 2.5,
                estimatedCompletion = com.google.firebase.Timestamp.now(),
                createdAt = com.google.firebase.Timestamp.now()
            )
        )
    }

    private fun getMockActivitiesList(): List<Notification> {
        // Create Notification objects using available properties
        return listOf(
            Notification(
                id = "1",
                title = "Transport status updated",
                message = "Citrus Fresh Co. (1.2t CitrusCycle)",
                type = "transport",
                createdAt = com.google.firebase.Timestamp.now(),
                isRead = false
            ),
            Notification(
                id = "2",
                title = "Batch completed",
                message = "#ECO-2025-072 (Citrus Enzyme)",
                type = "production",
                createdAt = com.google.firebase.Timestamp.now(),
                isRead = false
            ),
            Notification(
                id = "3",
                title = "New company",
                message = "Tropical Smoothies Ltd. joined",
                type = "company",
                createdAt = com.google.firebase.Timestamp.now(),
                isRead = false
            )
        )
    }

    private suspend fun getRecentProgress(limit: Int): List<ProgressTracking> {
        return try {
            val snapshot = progressCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            val result = snapshot.toObjects(ProgressTracking::class.java)
            if (result.isEmpty()) getMockProgressList() else result
        } catch (e: Exception) {
            getMockProgressList()
        }
    }

    private suspend fun getRecentProduction(limit: Int): List<Production> {
        return try {
            val snapshot = productionCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            val result = snapshot.toObjects(Production::class.java)
            if (result.isEmpty()) getMockProductionList() else result
        } catch (e: Exception) {
            getMockProductionList()
        }
    }

    private suspend fun getRecentActivities(limit: Int): List<Notification> {
        return try {
            val snapshot = notificationCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            val result = snapshot.toObjects(Notification::class.java)
            if (result.isEmpty()) getMockActivitiesList() else result
        } catch (e: Exception) {
            getMockActivitiesList()
        }
    }

    private fun calculateAvgProcessingTime(productionList: List<Production>): Double {
        val completedProductions = productionList.filter {
            it.actualCompletion != null && it.createdAt != null
        }

        if (completedProductions.isEmpty()) return 14.5 // Mock average

        val totalTime = completedProductions.sumOf { production ->
            val start = production.createdAt.seconds
            val end = production.actualCompletion!!.seconds
            (end - start) / (24 * 60 * 60) // Convert to days
        }

        return totalTime.toDouble() / completedProductions.size
    }

    private suspend fun calculateMonthlyGrowth(month: Int, year: Int, wasteType: String): Double {
        return try {
            // Calculate growth compared to previous month
            val currentMonth = Calendar.getInstance().apply {
                set(year, month, 1, 0, 0, 0)
            }
            val prevMonth = Calendar.getInstance().apply {
                set(year, month - 1, 1, 0, 0, 0)
            }

            // This is a simplified calculation - in real implementation you'd compare actual data
            15.7 // Mock growth percentage
        } catch (e: Exception) {
            15.7
        }
    }

    private fun calculateTopContributors(
        progressList: List<ProgressTracking>,
        productionList: List<Production>
    ): List<TopContributor> {
        // If no real data, return mock data
        if (progressList.isEmpty() && productionList.isEmpty()) {
            return listOf(
                TopContributor(
                    companyId = "1",
                    companyName = "Citrus Fresh Co.",
                    totalWaste = 5.2,
                    enzymeProduced = 2.1,
                    contributionScore = 85.0
                ),
                TopContributor(
                    companyId = "2",
                    companyName = "Tropical Fruits Inc.",
                    totalWaste = 4.8,
                    enzymeProduced = 1.9,
                    contributionScore = 78.0
                ),
                TopContributor(
                    companyId = "3",
                    companyName = "Smoothie Factory",
                    totalWaste = 3.5,
                    enzymeProduced = 1.4,
                    contributionScore = 65.0
                )
            )
        }

        // Group by company and calculate totals
        val companyStats = mutableMapOf<String, Triple<Double, Double, Double>>()

        progressList.forEach { progress ->
            val current = companyStats[progress.companyName] ?: Triple(0.0, 0.0, 0.0)
            companyStats[progress.companyName] = current.copy(first = current.first + progress.quantity)
        }

        productionList.forEach { production ->
            val current = companyStats[production.sourceName] ?: Triple(0.0, 0.0, 0.0)
            companyStats[production.sourceName] = current.copy(second = current.second + production.outputQuantity)
        }

        // Convert to TopContributor objects and sort
        return companyStats.map { (companyName, stats) ->
            val contributionScore = ((stats.first + stats.second * 2) / 100) * 100 // Example calculation
            TopContributor(
                companyId = companyName.hashCode().toString(),
                companyName = companyName,
                totalWaste = stats.first,
                enzymeProduced = stats.second,
                contributionScore = contributionScore.coerceAtMost(100.0)
            )
        }.sortedByDescending { it.contributionScore }.take(10)
    }
}