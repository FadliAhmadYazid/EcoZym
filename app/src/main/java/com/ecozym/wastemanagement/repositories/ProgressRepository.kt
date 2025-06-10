package com.ecozym.wastemanagement.repositories

import com.ecozym.wastemanagement.models.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ProgressRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val progressCollection = firestore.collection("progress")

    suspend fun getAllProgress(): List<ProgressTracking> {
        return try {
            val snapshot = progressCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(ProgressTracking::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getProgressByStatus(status: String): List<ProgressTracking> {
        return try {
            val snapshot = progressCollection
                .whereEqualTo("status", status)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(ProgressTracking::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getRecentProgress(limit: Int): List<ProgressTracking> {
        return try {
            val snapshot = progressCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            snapshot.toObjects(ProgressTracking::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDashboardStats(): DashboardStats {
        return try {
            val progressSnapshot = progressCollection.get().await()
            val progressList = progressSnapshot.toObjects(ProgressTracking::class.java)

            DashboardStats(
                totalWaste = progressList.sumOf { it.quantity },
                activeTransports = progressList.count { it.status in listOf("Scheduled", "In Transit") },
                completedBatches = progressList.count { it.status == "Delivered" }
            )
        } catch (e: Exception) {
            DashboardStats()
        }
    }

    suspend fun getEnvironmentalImpact(month: Int, year: Int, wasteType: String): EnvironmentalImpact {
        return try {
            // Implementation for calculating environmental impact
            // This would involve querying data for the specific month/year/waste type
            EnvironmentalImpact()
        } catch (e: Exception) {
            EnvironmentalImpact()
        }
    }

    suspend fun updateProgressStatus(progressId: String, newStatus: String) {
        try {
            progressCollection.document(progressId)
                .update("status", newStatus, "updatedAt", com.google.firebase.Timestamp.now())
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun searchProgress(query: String): List<ProgressTracking> {
        return try {
            // Simple search implementation - in a real app, you might use more sophisticated search
            val snapshot = progressCollection
                .orderBy("companyName")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .await()
            snapshot.toObjects(ProgressTracking::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}