package com.ecozym.wastemanagement.repositories

import com.ecozym.wastemanagement.models.User
import com.ecozym.wastemanagement.utils.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("User ID not found")

            val userDoc = firestore.collection("users").document(uid).get().await()
            if (userDoc.exists()) {
                val user = userDoc.toObject(User::class.java) ?: throw Exception("User data not found")

                if (user.status == "pending") {
                    auth.signOut()
                    throw Exception("Your account is pending approval")
                }

                if (user.status == "rejected") {
                    auth.signOut()
                    throw Exception("Your account has been rejected")
                }

                Result.Success(user)
            } else {
                auth.signOut()
                throw Exception("User profile not found")
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun createUserWithEmailAndPassword(userData: Map<String, Any>): Result<String> {
        return try {
            val email = userData["email"] as? String ?: throw Exception("Email is required")
            val password = userData["password"] as? String ?: throw Exception("Password is required")

            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("Failed to create user")

            // Create User object from userData
            val user = User(
                uid = uid,
                email = email,
                role = userData["role"] as? String ?: "",
                status = "pending",
                companyName = userData["companyName"] as? String,
                industryType = userData["industryType"] as? String,
                address = userData["address"] as? String,
                phone = userData["phoneNumber"] as? String, // Changed from phoneNumber to phone
                documentUrl = userData["documentUrl"] as? String,
                licenseNumber = userData["businessLicense"] as? String, // Changed from businessLicense to licenseNumber
                profileImageUrl = userData["profileImageUrl"] as? String,
                name = userData["name"] as? String,
                adminRole = userData["adminRole"] as? String,
                vehicleType = userData["vehicleType"] as? String,
                licensePlate = userData["licensePlate"] as? String
            )

            firestore.collection("users").document(uid).set(user).await()

            // Sign out until approved
            auth.signOut()

            Result.Success("Registration successful. Please wait for approval.")
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun getCurrentUser(): User? {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                if (userDoc.exists()) {
                    userDoc.toObject(User::class.java)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun getUserRole(uid: String): String? {
        return try {
            val userDoc = firestore.collection("users").document(uid).get().await()
            if (userDoc.exists()) {
                val user = userDoc.toObject(User::class.java)
                user?.role
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun resetPassword(email: String): Result<String> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.Success("Password reset email sent")
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun updateUserStatus(uid: String, status: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(uid)
                .update("status", status)
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val querySnapshot = firestore.collection("users").get().await()
            val users = querySnapshot.documents.mapNotNull {
                it.toObject(User::class.java)
            }
            Result.Success(users)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun getUsersByRole(role: String): Result<List<User>> {
        return try {
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("role", role)
                .get()
                .await()
            val users = querySnapshot.documents.mapNotNull {
                it.toObject(User::class.java)
            }
            Result.Success(users)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun getUsersByStatus(status: String): Result<List<User>> {
        return try {
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("status", status)
                .get()
                .await()
            val users = querySnapshot.documents.mapNotNull {
                it.toObject(User::class.java)
            }
            Result.Success(users)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}