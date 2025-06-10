package com.ecozym.wastemanagement.repositories

import android.util.Log
import com.ecozym.wastemanagement.models.User
import com.ecozym.wastemanagement.models.PartnerStats
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PartnerRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")
    private val TAG = "PartnerRepository"

    // Job untuk tracking coroutines yang berjalan
    private var repositoryJob = SupervisorJob()
    private val repositoryScope = CoroutineScope(Dispatchers.IO + repositoryJob)

    suspend fun getPartnerStats(): PartnerStats = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Loading partner stats...")

            // Cek apakah job masih aktif
            ensureActive()

            val snapshot = usersCollection.get().await()

            // Cek lagi setelah network call
            ensureActive()

            val users = snapshot.toObjects(User::class.java)

            Log.d(TAG, "Total users found: ${users.size}")

            val pendingCount = users.count { it.status == "pending" }
            val registeredCount = users.count { it.status == "approved" || it.status == "active" }
            val adminCount = users.count { it.role == "admin" || it.role == "super_admin" }
            val companyCount = users.count { it.role == "company" }
            val driverCount = users.count { it.role == "driver" }

            Log.d(TAG, "Pending: $pendingCount, Registered: $registeredCount, Admin: $adminCount")

            // Calculate this month's stats
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startOfMonth = com.google.firebase.Timestamp(calendar.time)

            val thisMonthUsers = users.filter {
                it.updatedAt.seconds >= startOfMonth.seconds
            }
            val approvedThisMonth = thisMonthUsers.count { it.status == "approved" }
            val rejectedThisMonth = thisMonthUsers.count { it.status == "rejected" }

            PartnerStats(
                pendingCount = pendingCount,
                registeredCount = registeredCount,
                adminCount = adminCount,
                companyCount = companyCount,
                driverCount = driverCount,
                totalCount = users.size,
                approvedThisMonth = approvedThisMonth,
                rejectedThisMonth = rejectedThisMonth
            )
        } catch (e: CancellationException) {
            Log.w(TAG, "Partner stats loading cancelled")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error loading partner stats", e)
            PartnerStats()
        }
    }

    suspend fun getPendingPartners(): List<User> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Loading pending partners...")

            ensureActive()

            // Try with orderBy first (for when index is ready)
            try {
                val snapshot = usersCollection
                    .whereEqualTo("status", "pending")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                ensureActive()

                val partners = snapshot.toObjects(User::class.java)
                Log.d(TAG, "Found ${partners.size} pending partners (with orderBy)")

                partners.take(3).forEach { partner ->
                    Log.d(TAG, "Pending partner: ${partner.companyName ?: partner.name} - Status: ${partner.status} - Role: ${partner.role}")
                }

                return@withContext partners
            } catch (indexException: Exception) {
                // Fallback to manual sorting if index not ready
                Log.w(TAG, "OrderBy failed, using manual sorting: ${indexException.message}")

                ensureActive()

                val snapshot = usersCollection
                    .whereEqualTo("status", "pending")
                    .get()
                    .await()

                ensureActive()

                val partners = snapshot.toObjects(User::class.java)
                val sortedPartners = partners.sortedByDescending { it.createdAt.seconds }

                Log.d(TAG, "Found ${sortedPartners.size} pending partners (manual sort)")

                sortedPartners.take(3).forEach { partner ->
                    Log.d(TAG, "Pending partner: ${partner.companyName ?: partner.name} - Status: ${partner.status} - Role: ${partner.role}")
                }

                return@withContext sortedPartners
            }
        } catch (e: CancellationException) {
            Log.w(TAG, "Pending partners loading cancelled")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error loading pending partners", e)
            emptyList()
        }
    }

    suspend fun getRegisteredPartners(filterType: String? = null): List<User> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Loading registered partners with filter: $filterType")

            ensureActive()

            // Try with orderBy first (for when index is ready)
            try {
                var query = usersCollection
                    .whereIn("status", listOf("approved", "active"))

                // Apply role filter if specified
                if (filterType != null && filterType != "all") {
                    query = query.whereEqualTo("role", filterType)
                }

                // Add orderBy
                query = query.orderBy("createdAt", Query.Direction.DESCENDING)

                val snapshot = query.get().await()

                ensureActive()

                val partners = snapshot.toObjects(User::class.java)

                Log.d(TAG, "Found ${partners.size} registered partners (with orderBy)")

                partners.take(3).forEach { partner ->
                    Log.d(TAG, "Registered partner: ${partner.companyName ?: partner.name} - Status: ${partner.status} - Role: ${partner.role}")
                }

                return@withContext partners
            } catch (indexException: Exception) {
                // Fallback to manual sorting if index not ready
                Log.w(TAG, "OrderBy failed, using manual sorting: ${indexException.message}")

                ensureActive()

                var query = usersCollection
                    .whereIn("status", listOf("approved", "active"))

                // Apply role filter if specified
                if (filterType != null && filterType != "all") {
                    query = query.whereEqualTo("role", filterType)
                }

                val snapshot = query.get().await()

                ensureActive()

                val partners = snapshot.toObjects(User::class.java)
                val sortedPartners = partners.sortedByDescending { it.createdAt.seconds }

                Log.d(TAG, "Found ${sortedPartners.size} registered partners (manual sort)")

                sortedPartners.take(3).forEach { partner ->
                    Log.d(TAG, "Registered partner: ${partner.companyName ?: partner.name} - Status: ${partner.status} - Role: ${partner.role}")
                }

                return@withContext sortedPartners
            }
        } catch (e: CancellationException) {
            Log.w(TAG, "Registered partners loading cancelled")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error loading registered partners", e)
            emptyList()
        }
    }

    suspend fun approvePartner(partnerId: String) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Approving partner: $partnerId")
            ensureActive()

            usersCollection.document(partnerId)
                .update(
                    mapOf(
                        "status" to "approved",
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()
            Log.d(TAG, "Partner approved successfully")
        } catch (e: CancellationException) {
            Log.w(TAG, "Partner approval cancelled")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error approving partner", e)
            throw e
        }
    }

    suspend fun rejectPartner(partnerId: String) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Rejecting partner: $partnerId")
            ensureActive()

            usersCollection.document(partnerId)
                .update(
                    mapOf(
                        "status" to "rejected",
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()
            Log.d(TAG, "Partner rejected successfully")
        } catch (e: CancellationException) {
            Log.w(TAG, "Partner rejection cancelled")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error rejecting partner", e)
            throw e
        }
    }

    suspend fun deletePartner(partnerId: String) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Deleting partner: $partnerId")
            ensureActive()

            usersCollection.document(partnerId).delete().await()
            Log.d(TAG, "Partner deleted successfully")
        } catch (e: CancellationException) {
            Log.w(TAG, "Partner deletion cancelled")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting partner", e)
            throw e
        }
    }

    suspend fun searchPendingPartners(query: String): List<User> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Searching pending partners with query: $query")
            ensureActive()

            if (query.isBlank()) {
                return@withContext getPendingPartners()
            }

            val snapshot = usersCollection
                .whereEqualTo("status", "pending")
                .get()  // No orderBy for search to avoid index issues
                .await()

            ensureActive()

            val allPartners = snapshot.toObjects(User::class.java)
            Log.d(TAG, "Found ${allPartners.size} pending partners before search")

            // Filter locally based on query and sort
            val filteredPartners = allPartners.filter { user ->
                user.companyName?.contains(query, ignoreCase = true) == true ||
                    user.name?.contains(query, ignoreCase = true) == true ||
                    user.email.contains(query, ignoreCase = true)
            }.sortedByDescending { it.createdAt.seconds }

            Log.d(TAG, "Found ${filteredPartners.size} pending partners after search")
            filteredPartners
        } catch (e: CancellationException) {
            Log.w(TAG, "Pending partners search cancelled")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error searching pending partners", e)
            emptyList()
        }
    }

    suspend fun searchRegisteredPartners(query: String): List<User> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Searching registered partners with query: $query")
            ensureActive()

            if (query.isBlank()) {
                return@withContext getRegisteredPartners()
            }

            val snapshot = usersCollection
                .whereIn("status", listOf("approved", "active"))
                .get()  // No orderBy for search to avoid index issues
                .await()

            ensureActive()

            val allPartners = snapshot.toObjects(User::class.java)
            Log.d(TAG, "Found ${allPartners.size} registered partners before search")

            // Filter locally based on query and sort
            val filteredPartners = allPartners.filter { user ->
                user.companyName?.contains(query, ignoreCase = true) == true ||
                    user.name?.contains(query, ignoreCase = true) == true ||
                    user.email.contains(query, ignoreCase = true)
            }.sortedByDescending { it.createdAt.seconds }

            Log.d(TAG, "Found ${filteredPartners.size} registered partners after search")
            filteredPartners
        } catch (e: CancellationException) {
            Log.w(TAG, "Registered partners search cancelled")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error searching registered partners", e)
            emptyList()
        }
    }

    suspend fun addPartner(partnerData: Map<String, Any>) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Adding new partner")
            ensureActive()

            val partnerId = usersCollection.document().id
            val userData = partnerData.toMutableMap().apply {
                put("uid", partnerId)
                put("createdAt", com.google.firebase.Timestamp.now())
                put("updatedAt", com.google.firebase.Timestamp.now())
                put("status", "approved") // Direct add means approved
            }

            usersCollection.document(partnerId).set(userData).await()
            Log.d(TAG, "Partner added successfully")
        } catch (e: CancellationException) {
            Log.w(TAG, "Partner addition cancelled")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error adding partner", e)
            throw e
        }
    }

    suspend fun updatePartner(partnerId: String, partnerData: Map<String, Any>) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating partner: $partnerId")
            ensureActive()

            val updateData = partnerData.toMutableMap().apply {
                put("updatedAt", com.google.firebase.Timestamp.now())
            }

            usersCollection.document(partnerId).update(updateData).await()
            Log.d(TAG, "Partner updated successfully")
        } catch (e: CancellationException) {
            Log.w(TAG, "Partner update cancelled")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error updating partner", e)
            throw e
        }
    }

    suspend fun getPartnerById(partnerId: String): User? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting partner by ID: $partnerId")
            ensureActive()

            val snapshot = usersCollection.document(partnerId).get().await()
            snapshot.toObject(User::class.java)
        } catch (e: CancellationException) {
            Log.w(TAG, "Get partner by ID cancelled")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error getting partner by ID", e)
            null
        }
    }

    // Tambahkan method untuk cleanup
    fun cancelAllOperations() {
        Log.d(TAG, "Cancelling all repository operations")
        if (repositoryJob.isActive) {
            repositoryJob.cancel()
            repositoryJob = SupervisorJob()
        }
    }
}