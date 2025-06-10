package com.ecozym.wastemanagement.repositories

import com.ecozym.wastemanagement.models.Admin
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class AdminRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val adminsCollection = firestore.collection("admins")

    suspend fun getAdminProfile(adminId: String): Admin {
        return try {
            val document = adminsCollection.document(adminId).get().await()
            document.toObject<Admin>() ?: throw Exception("Admin not found")
        } catch (e: Exception) {
            throw Exception("Failed to fetch admin profile: ${e.message}")
        }
    }

    suspend fun updateAdminProfile(admin: Admin) {
        try {
            val updatedAdmin = admin.copy(
                updatedAt = com.google.firebase.Timestamp.now()
            )
            adminsCollection.document(admin.uid).set(updatedAdmin).await()
        } catch (e: Exception) {
            throw Exception("Failed to update admin profile: ${e.message}")
        }
    }

    suspend fun checkPermission(adminId: String, permission: String): Boolean {
        return try {
            val admin = getAdminProfile(adminId)
            admin.permissions.contains(permission) && admin.isActive
        } catch (e: Exception) {
            false
        }
    }

    suspend fun createAdmin(admin: Admin) {
        try {
            adminsCollection.document(admin.uid).set(admin).await()
        } catch (e: Exception) {
            throw Exception("Failed to create admin: ${e.message}")
        }
    }

    suspend fun getAllAdmins(): List<Admin> {
        return try {
            val querySnapshot = adminsCollection.get().await()
            querySnapshot.documents.mapNotNull { it.toObject<Admin>() }
        } catch (e: Exception) {
            throw Exception("Failed to fetch admins: ${e.message}")
        }
    }

    suspend fun deactivateAdmin(adminId: String) {
        try {
            adminsCollection.document(adminId)
                .update("isActive", false, "updatedAt", com.google.firebase.Timestamp.now())
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to deactivate admin: ${e.message}")
        }
    }
}