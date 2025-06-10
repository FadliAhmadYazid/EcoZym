package com.ecozym.wastemanagement.activities

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ecozym.wastemanagement.R
import com.ecozym.wastemanagement.databinding.ActivitySignupStep3Binding
import com.ecozym.wastemanagement.utils.CloudinaryHelper
import com.ecozym.wastemanagement.utils.SignUpData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpStep3Activity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupStep3Binding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var cloudinaryHelper: CloudinaryHelper
    private var selectedDocumentUri: Uri? = null
    private var progressDialog: ProgressDialog? = null

    companion object {
        private const val TAG = "SignUpStep3Activity"
        private const val STORAGE_PERMISSION_CODE = 101
    }

    private val documentPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedDocumentUri = uri
                binding.ivUpload.setImageResource(R.drawable.document) // Add this drawable or use existing
                Toast.makeText(this, "Document selected successfully", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Document selected: $uri")
            }
        } else {
            Log.d(TAG, "Document selection cancelled")
        }
    }

    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            selectDocument()
        } else {
            // Check which permissions were denied
            val deniedPermissions = permissions.filter { !it.value }.keys
            Log.d(TAG, "Denied permissions: $deniedPermissions")

            // Check if user selected "Don't ask again"
            val shouldShowRationale = deniedPermissions.any {
                ActivityCompat.shouldShowRequestPermissionRationale(this, it)
            }

            if (!shouldShowRationale) {
                showPermissionSettingsDialog()
            } else {
                showPermissionRationaleDialog()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupStep3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize CloudinaryHelper (Cloudinary already initialized in Application)
        try {
            cloudinaryHelper = CloudinaryHelper()
            Log.d(TAG, "CloudinaryHelper created successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create CloudinaryHelper", e)
            Toast.makeText(this, "Upload service initialization failed", Toast.LENGTH_SHORT).show()
        }

        setupClickListeners()

        // Initialize progress dialog
        progressDialog = ProgressDialog(this).apply {
            setMessage("Processing...")
            setCancelable(false)
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnPrevious.setOnClickListener {
            finish()
        }

        binding.uploadLayout.setOnClickListener {
            checkPermissionAndSelectDocument()
        }

        binding.btnNext.setOnClickListener {
            if (selectedDocumentUri != null) {
                registerUser()
            } else {
                Toast.makeText(this, "Please select SIUP/TDP document first", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun checkPermissionAndSelectDocument() {
        val permissions = getRequiredPermissions()

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        Log.d(TAG, "Required permissions: ${permissions.joinToString()}")
        Log.d(TAG, "Permissions to request: ${permissionsToRequest.joinToString()}")

        if (permissionsToRequest.isEmpty()) {
            selectDocument()
        } else {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun getRequiredPermissions(): List<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+ (API 33+)
                listOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                // Android 10-12 (API 29-32)
                listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            else -> {
                // Android 6-9 (API 23-28)
                listOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }
    }

    private fun selectDocument() {
        try {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*" // Allow all file types initially
                val mimeTypes = arrayOf("application/pdf", "image/jpeg", "image/png", "image/jpg")
                putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                addCategory(Intent.CATEGORY_OPENABLE)

                // For Android 11+ (API 30+), add these flags for better compatibility
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                }
            }

            // Check if there's an app that can handle this intent
            if (intent.resolveActivity(packageManager) != null) {
                documentPickerLauncher.launch(intent)
            } else {
                // Fallback to document picker
                selectDocumentFallback()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening file picker", e)
            selectDocumentFallback()
        }
    }

    private fun selectDocumentFallback() {
        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"
                val mimeTypes = arrayOf("application/pdf", "image/jpeg", "image/png", "image/jpg")
                putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                addCategory(Intent.CATEGORY_OPENABLE)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            if (intent.resolveActivity(packageManager) != null) {
                documentPickerLauncher.launch(intent)
            } else {
                Toast.makeText(this, "No file manager app found. Please install a file manager.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in fallback file picker", e)
            Toast.makeText(this, "Unable to open file picker: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Storage Permission Required")
            .setMessage("This app needs storage permission to upload your SIUP/TDP document. Without this permission, you cannot complete the registration process.")
            .setPositiveButton("Grant Permission") { _, _ ->
                checkPermissionAndSelectDocument()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "Storage permission is required to upload documents", Toast.LENGTH_LONG).show()
            }
            .setCancelable(false)
            .show()
    }

    private fun showPermissionSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Storage permission is required to upload documents. Please enable it in app settings.\n\nGo to: Settings > Apps > EcoZym > Permissions > Storage")
            .setPositiveButton("Open Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "Storage permission is required to upload documents", Toast.LENGTH_LONG).show()
            }
            .setCancelable(false)
            .show()
    }

    private fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening app settings", e)
            Toast.makeText(this, "Unable to open app settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerUser() {
        if (SignUpData.email.isEmpty() || SignUpData.password.isEmpty()) {
            Toast.makeText(this, "Registration data is missing. Please start over.", Toast.LENGTH_LONG).show()
            return
        }

        showProgressDialog("Creating account...")
        binding.btnNext.isEnabled = false

        Log.d(TAG, "Starting user registration for email: ${SignUpData.email}")

        // Create Firebase Auth user
        auth.createUserWithEmailAndPassword(SignUpData.email, SignUpData.password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        Log.d(TAG, "Firebase user created successfully: ${it.uid}")
                        uploadDocumentAndSaveUser(it.uid)
                    } ?: run {
                        hideProgressDialog()
                        showError("Failed to get user information")
                    }
                } else {
                    hideProgressDialog()
                    val exception = task.exception
                    val errorMessage = when {
                        exception is com.google.firebase.auth.FirebaseAuthUserCollisionException -> {
                            "Email already registered. Please use a different email or try logging in."
                        }
                        exception is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> {
                            "Password is too weak. Please use a stronger password."
                        }
                        exception is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                            "Invalid email format. Please check your email address."
                        }
                        else -> exception?.message ?: "Registration failed"
                    }
                    Log.e(TAG, "Firebase registration failed", exception)
                    showError(errorMessage)
                }
            }
    }

    private fun uploadDocumentAndSaveUser(uid: String) {
        selectedDocumentUri?.let { uri ->
            showProgressDialog("Uploading document...")
            Log.d(TAG, "Starting document upload for URI: $uri")

            cloudinaryHelper.uploadDocument(uri, this) { documentUrl ->
                runOnUiThread {
                    if (documentUrl != null) {
                        Log.d(TAG, "Document uploaded successfully: $documentUrl")
                        saveUserToFirestore(uid, documentUrl)
                    } else {
                        hideProgressDialog()
                        showError("Failed to upload document. Please try again.")
                        Log.e(TAG, "Document upload failed")
                    }
                }
            }
        } ?: run {
            hideProgressDialog()
            showError("No document selected")
        }
    }

    private fun saveUserToFirestore(uid: String, documentUrl: String) {
        showProgressDialog("Saving user data...")

        val userData = hashMapOf(
            "uid" to uid,
            "email" to SignUpData.email,
            "companyName" to SignUpData.companyName,
            "industryType" to SignUpData.industryType,
            "address" to SignUpData.address,
            "phone" to SignUpData.phoneNumber, // Changed from phoneNumber to phone to match User model
            "documentUrl" to documentUrl,
            "role" to "company",
            "status" to "pending",
            "createdAt" to com.google.firebase.Timestamp.now(),
            "updatedAt" to com.google.firebase.Timestamp.now()
        )

        Log.d(TAG, "Saving user data to Firestore for UID: $uid")

        firestore.collection("users")
            .document(uid)
            .set(userData)
            .addOnSuccessListener {
                Log.d(TAG, "User data saved successfully")
                hideProgressDialog()

                // Clear signup data
                SignUpData.clear()

                // Sign out user until approved
                auth.signOut()

                val intent = Intent(this, CongratulationActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { exception ->
                hideProgressDialog()
                Log.e(TAG, "Failed to save user data", exception)
                showError("Failed to save user data: ${exception.message}")
            }
    }

    private fun showProgressDialog(message: String) {
        progressDialog?.setMessage(message)
        progressDialog?.show()
    }

    private fun hideProgressDialog() {
        progressDialog?.dismiss()
        binding.btnNext.isEnabled = true
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        binding.btnNext.isEnabled = true
    }

    override fun onDestroy() {
        super.onDestroy()
        progressDialog?.dismiss()
    }

    override fun onResume() {
        super.onResume()
        // Check if user came back from settings and now has permission
        if (selectedDocumentUri == null) {
            val permissions = getRequiredPermissions()
            val hasAllPermissions = permissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }

            if (hasAllPermissions) {
                Log.d(TAG, "Permissions granted after returning from settings")
                // Don't automatically open file picker, let user click the upload button
            }
        }
    }
}