package com.ecozym.wastemanagement.repositories

import android.util.Log
import com.ecozym.wastemanagement.models.WasteRegistration
import com.ecozym.wastemanagement.models.ProgressTracking
import com.ecozym.wastemanagement.models.Notification
import com.ecozym.wastemanagement.utils.Result
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WasteRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val TAG = "WasteRepository"

    // DEBUG VERSION - dengan logging detail
    suspend fun getPendingWasteRegistrations(): Result<List<WasteRegistration>> {
        return try {
            Log.d(TAG, "🔍 Starting to fetch pending waste registrations...")

            // Get ALL documents first for debugging
            val allQuery = firestore.collection("waste_registrations")
                .get()
                .await()

            Log.d(TAG, "📊 Total documents in collection: ${allQuery.documents.size}")

            // Log each document
            allQuery.documents.forEachIndexed { index, doc ->
                Log.d(TAG, "📄 Document $index:")
                Log.d(TAG, "   ID: ${doc.id}")
                Log.d(TAG, "   Data: ${doc.data}")
                Log.d(TAG, "   Status: ${doc.getString("status")}")
                Log.d(TAG, "   CompanyId: ${doc.getString("companyId")}")
                Log.d(TAG, "   WasteType: ${doc.getString("wasteType")}")
            }

            // Now filter by status = "pending"
            val pendingQuery = firestore.collection("waste_registrations")
                .whereEqualTo("status", "pending")
                .get()
                .await()

            Log.d(TAG, "📊 Documents with status='pending': ${pendingQuery.documents.size}")

            val registrations = mutableListOf<WasteRegistration>()

            pendingQuery.documents.forEachIndexed { index, doc ->
                Log.d(TAG, "🔍 Processing pending document $index:")
                Log.d(TAG, "   Document ID: ${doc.id}")
                Log.d(TAG, "   Raw data: ${doc.data}")

                try {
                    val registration = doc.toObject(WasteRegistration::class.java)
                    if (registration != null) {
                        val registrationWithId = registration.copy(id = doc.id)
                        registrations.add(registrationWithId)

                        Log.d(TAG, "✅ Successfully parsed registration:")
                        Log.d(TAG, "   ID: ${registrationWithId.id}")
                        Log.d(TAG, "   CompanyId: ${registrationWithId.companyId}")
                        Log.d(TAG, "   CompanyName: '${registrationWithId.companyName}'")
                        Log.d(TAG, "   WasteType: ${registrationWithId.wasteType}")
                        Log.d(TAG, "   Status: ${registrationWithId.status}")
                        Log.d(TAG, "   Quantity: ${registrationWithId.quantity}")
                        Log.d(TAG, "   CreatedAt: ${registrationWithId.createdAt}")
                    } else {
                        Log.w(TAG, "❌ Failed to parse document to WasteRegistration")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error parsing document ${doc.id}", e)
                }
            }

            // Sort by createdAt descending
            val sortedRegistrations = registrations.sortedByDescending { it.createdAt }

            Log.d(TAG, "📊 Final result: ${sortedRegistrations.size} pending registrations")
            sortedRegistrations.forEachIndexed { index, reg ->
                Log.d(TAG, "📋 Registration $index: ${reg.wasteType} - ${reg.companyName} (${reg.status})")
            }

            Result.Success(sortedRegistrations)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting pending waste registrations", e)
            Result.Failure(e)
        }
    }

    // Original methods remain the same...
    suspend fun registerWaste(wasteRegistration: WasteRegistration): Result<String> {
        return try {
            Log.d(TAG, "Registering waste for company: ${wasteRegistration.companyId}")

            // Save waste registration
            val docRef = firestore.collection("waste_registrations").add(wasteRegistration).await()
            Log.d(TAG, "Waste registration saved with ID: ${docRef.id}")

            // Create notification for admin
            createAdminNotification(wasteRegistration, docRef.id)

            Result.Success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error registering waste", e)
            Result.Failure(e)
        }
    }

    private suspend fun createAdminNotification(wasteRegistration: WasteRegistration, registrationId: String) {
        try {
            val notification = Notification(
                id = "",
                userId = "admin",
                type = "admin",
                title = "New Waste Registration",
                message = "New waste registration from ${wasteRegistration.companyName} - ${wasteRegistration.wasteType} (${wasteRegistration.quantity} kg)",
                data = mapOf(
                    "registrationId" to registrationId,
                    "companyId" to wasteRegistration.companyId,
                    "wasteType" to wasteRegistration.wasteType,
                    "quantity" to wasteRegistration.quantity.toString(),
                    "action" to "waste_approval"
                ),
                isRead = false,
                read = false,
                createdAt = Timestamp.now()
            )

            firestore.collection("notifications").add(notification).await()
            Log.d(TAG, "Admin notification created for waste registration: $registrationId")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating admin notification", e)
        }
    }

    suspend fun approveWasteRegistration(registrationId: String, adminId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Approving waste registration: $registrationId")

            val wasteDoc = firestore.collection("waste_registrations").document(registrationId).get().await()
            val wasteRegistration = wasteDoc.toObject(WasteRegistration::class.java)
                ?: return Result.Failure(Exception("Waste registration not found"))

            firestore.collection("waste_registrations")
                .document(registrationId)
                .update(
                    "status", "approved",
                    "updatedAt", Timestamp.now()
                ).await()

            createProgressTracking(wasteRegistration.copy(id = registrationId))
            createCompanyNotification(wasteRegistration, "approved")

            Log.d(TAG, "Waste registration approved successfully: $registrationId")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error approving waste registration", e)
            Result.Failure(e)
        }
    }

    suspend fun rejectWasteRegistration(registrationId: String, adminId: String, reason: String? = null): Result<Unit> {
        return try {
            Log.d(TAG, "Rejecting waste registration: $registrationId")

            val wasteDoc = firestore.collection("waste_registrations").document(registrationId).get().await()
            val wasteRegistration = wasteDoc.toObject(WasteRegistration::class.java)
                ?: return Result.Failure(Exception("Waste registration not found"))

            val updateData = mutableMapOf<String, Any>(
                "status" to "rejected",
                "updatedAt" to Timestamp.now()
            )
            if (reason != null) {
                updateData["rejectionReason"] = reason
            }

            firestore.collection("waste_registrations")
                .document(registrationId)
                .update(updateData)
                .await()

            createCompanyNotification(wasteRegistration, "rejected", reason)

            Log.d(TAG, "Waste registration rejected successfully: $registrationId")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error rejecting waste registration", e)
            Result.Failure(e)
        }
    }

    private suspend fun createProgressTracking(wasteRegistration: WasteRegistration) {
        try {
            val progressTracking = ProgressTracking(
                id = "",
                companyId = wasteRegistration.companyId,
                companyName = wasteRegistration.companyName,
                wasteType = wasteRegistration.wasteType,
                transportId = generateTransportId(),
                quantity = wasteRegistration.quantity,
                status = "In Transit",
                driverName = "Assigned Driver",
                vehicleType = "Truck",
                licensePlate = "B 1234 CD",
                pickupDate = Timestamp.now(),
                estimatedArrival = Timestamp(Timestamp.now().seconds + 3600, 0),
                estimatedReturn = Timestamp(Timestamp.now().seconds + 7200, 0),
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            firestore.collection("progress_tracking").add(progressTracking).await()
            Log.d(TAG, "Progress tracking created for approved waste registration")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating progress tracking", e)
        }
    }

    private suspend fun createCompanyNotification(wasteRegistration: WasteRegistration, status: String, reason: String? = null) {
        try {
            val title = when (status) {
                "approved" -> "Waste Registration Approved"
                "rejected" -> "Waste Registration Rejected"
                else -> "Waste Registration Update"
            }

            val message = when (status) {
                "approved" -> "Your waste registration for ${wasteRegistration.wasteType} (${wasteRegistration.quantity} kg) has been approved and is now in transit."
                "rejected" -> "Your waste registration for ${wasteRegistration.wasteType} (${wasteRegistration.quantity} kg) has been rejected." +
                    if (reason != null) " Reason: $reason" else ""
                else -> "Status updated for your waste registration"
            }

            val notification = Notification(
                id = "",
                userId = wasteRegistration.companyId,
                type = "company",
                title = title,
                message = message,
                data = mapOf(
                    "registrationId" to wasteRegistration.id,
                    "status" to status,
                    "wasteType" to wasteRegistration.wasteType,
                    "quantity" to wasteRegistration.quantity.toString()
                ),
                isRead = false,
                read = false,
                createdAt = Timestamp.now()
            )

            firestore.collection("notifications").add(notification).await()
            Log.d(TAG, "Company notification created for waste registration status: $status")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating company notification", e)
        }
    }

    private fun generateTransportId(): String {
        return "TRP${System.currentTimeMillis()}"
    }

    // Existing methods...
    suspend fun getWasteRegistrations(companyId: String): Result<List<WasteRegistration>> {
        return try {
            val query = firestore.collection("waste_registrations")
                .whereEqualTo("companyId", companyId)
                .get()
                .await()

            val registrations = query.documents.mapNotNull { doc ->
                doc.toObject(WasteRegistration::class.java)?.copy(id = doc.id)
            }.sortedByDescending { it.createdAt }

            Result.Success(registrations)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun getOngoingTracking(companyId: String): Result<List<ProgressTracking>> {
        return try {
            val query = firestore.collection("progress_tracking")
                .whereEqualTo("companyId", companyId)
                .get()
                .await()

            val trackingList = query.documents.mapNotNull { doc ->
                doc.toObject(ProgressTracking::class.java)?.copy(id = doc.id)
            }.filter { it.status in listOf("Scheduled", "In Transit", "Processing", "Returning") }
                .sortedByDescending { it.createdAt }

            Result.Success(trackingList)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun getTrackingHistory(companyId: String): Result<List<ProgressTracking>> {
        return try {
            val query = firestore.collection("progress_tracking")
                .whereEqualTo("companyId", companyId)
                .whereEqualTo("status", "Delivered")
                .get()
                .await()

            val trackingList = query.documents.mapNotNull { doc ->
                doc.toObject(ProgressTracking::class.java)?.copy(id = doc.id)
            }.sortedByDescending { it.createdAt }

            Result.Success(trackingList)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun updateWasteStatus(wasteId: String, status: String): Result<Unit> {
        return try {
            firestore.collection("waste_registrations")
                .document(wasteId)
                .update("status", status)
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun searchWasteRegistrations(companyId: String, query: String): Result<List<WasteRegistration>> {
        return try {
            val registrations = firestore.collection("waste_registrations")
                .whereEqualTo("companyId", companyId)
                .get()
                .await()

            val filteredRegistrations = registrations.documents.mapNotNull { doc ->
                doc.toObject(WasteRegistration::class.java)?.copy(id = doc.id)
            }.filter { registration ->
                registration.wasteType.contains(query, ignoreCase = true) ||
                    registration.location.contains(query, ignoreCase = true)
            }

            Result.Success(filteredRegistrations)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}