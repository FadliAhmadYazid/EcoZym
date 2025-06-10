package com.ecozym.wastemanagement.repositories

import com.ecozym.wastemanagement.models.Production
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ProductionRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val productionCollection = firestore.collection("production")

    suspend fun getAllProduction(): List<Production> {
        return try {
            val snapshot = productionCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(Production::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getProductionByStatus(status: String): List<Production> {
        return try {
            val snapshot = productionCollection
                .whereEqualTo("status", status)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(Production::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getRecentProduction(limit: Int): List<Production> {
        return try {
            val snapshot = productionCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            snapshot.toObjects(Production::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateProductionStatus(productionId: String, newStatus: String) {
        try {
            productionCollection.document(productionId)
                .update("status", newStatus, "updatedAt", com.google.firebase.Timestamp.now())
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateProductionProgress(productionId: String, progress: Int) {
        try {
            productionCollection.document(productionId)
                .update("progress", progress, "updatedAt", com.google.firebase.Timestamp.now())
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun searchProduction(query: String): List<Production> {
        return try {
            val snapshot = productionCollection
                .orderBy("batchId")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .await()
            snapshot.toObjects(Production::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}