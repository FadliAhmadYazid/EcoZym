package com.ecozym.wastemanagement.repositories

import com.ecozym.wastemanagement.models.Notification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class NotificationRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val notificationCollection = firestore.collection("notifications")

    suspend fun getAllNotifications(): List<Notification> {
        return try {
            val snapshot = notificationCollection
                .get()
                .await()
            snapshot.toObjects(Notification::class.java).sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserNotifications(userId: String): List<Notification> {
        return try {
            val snapshot = notificationCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.toObjects(Notification::class.java).sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // SIMPLIFIED - Remove complex query that requires index
    suspend fun getAdminNotifications(): List<Notification> {
        return try {
            val snapshot = notificationCollection
                .whereEqualTo("type", "admin")
                .get()
                .await()
            snapshot.toObjects(Notification::class.java).sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getRecentActivity(limit: Int): List<Notification> {
        return try {
            val snapshot = notificationCollection
                .limit(limit.toLong())
                .get()
                .await()
            snapshot.toObjects(Notification::class.java).sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun markAsRead(notificationId: String) {
        try {
            notificationCollection.document(notificationId)
                .update(
                    "isRead", true,
                    "read", true, // Update both fields
                    "readAt", com.google.firebase.Timestamp.now()
                )
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun markAllAsRead(userId: String) {
        try {
            val snapshot = notificationCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            val batch = firestore.batch()
            snapshot.documents.forEach { document ->
                batch.update(
                    document.reference,
                    "isRead", true,
                    "read", true,
                    "readAt", com.google.firebase.Timestamp.now()
                )
            }
            batch.commit().await()
        } catch (e: Exception) {
            throw e
        }
    }
}