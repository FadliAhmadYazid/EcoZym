package com.ecozym.wastemanagement.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class CloudinaryHelper {

    companion object {
        private const val TAG = "CloudinaryHelper"
        private var isInitialized = false

        fun initCloudinary(context: Context) {
            if (isInitialized) {
                Log.d(TAG, "Cloudinary already initialized")
                return
            }

            try {
                val config = hashMapOf<String, String>()
                config["cloud_name"] = "dcc2qalvx"
                config["api_key"] = "617449676182451"
                config["api_secret"] = "ynykJjhdtJqWoZVMjogbwyNCSso"

                MediaManager.init(context, config)
                isInitialized = true
                Log.d(TAG, "Cloudinary initialized successfully")
            } catch (e: IllegalStateException) {
                // Already initialized
                isInitialized = true
                Log.d(TAG, "Cloudinary was already initialized")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Cloudinary", e)
                throw e
            }
        }

        fun uploadFile(
            file: File,
            onSuccess: (String) -> Unit,
            onError: (String) -> Unit
        ) {
            try {
                if (!file.exists() || !file.canRead()) {
                    onError("File does not exist or cannot be read")
                    return
                }

                Log.d(TAG, "Starting upload for file: ${file.absolutePath}, size: ${file.length()} bytes")

                MediaManager.get().upload(file.absolutePath)
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {
                            Log.d(TAG, "Upload started: $requestId")
                        }

                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                            Log.d(TAG, "Upload progress: $bytes/$totalBytes")
                        }

                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            val url = resultData["secure_url"] as? String
                            if (url != null) {
                                Log.d(TAG, "Upload successful: $url")
                                onSuccess(url)
                            } else {
                                Log.e(TAG, "Failed to get upload URL from result")
                                onError("Failed to get upload URL")
                            }
                        }

                        override fun onError(requestId: String, error: ErrorInfo) {
                            Log.e(TAG, "Upload error: ${error.description}")
                            onError(error.description)
                        }

                        override fun onReschedule(requestId: String, error: ErrorInfo) {
                            Log.w(TAG, "Upload rescheduled: ${error.description}")
                            onError("Upload rescheduled: ${error.description}")
                        }
                    })
                    .dispatch()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start upload", e)
                onError("Failed to start upload: ${e.message}")
            }
        }
    }

    // Instance method for image upload
    fun uploadImage(
        uri: Uri,
        context: Context,
        callback: (String?) -> Unit
    ) {
        try {
            Log.d(TAG, "Starting image upload for URI: $uri")
            // Convert URI to File
            val file = createFileFromUri(uri, context, "image")
            if (file != null) {
                Log.d(TAG, "File created successfully: ${file.absolutePath}")
                // Upload using the companion object method
                uploadFile(
                    file = file,
                    onSuccess = { url ->
                        Log.d(TAG, "Image upload successful")
                        callback(url)
                        // Clean up temporary file
                        cleanupFile(file)
                    },
                    onError = { error ->
                        Log.e(TAG, "Image upload failed: $error")
                        callback(null)
                        // Clean up temporary file
                        cleanupFile(file)
                    }
                )
            } else {
                Log.e(TAG, "Failed to create file from URI")
                callback(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in uploadImage", e)
            callback(null)
        }
    }

    // Instance method for document upload
    fun uploadDocument(
        uri: Uri,
        context: Context,
        callback: (String?) -> Unit
    ) {
        try {
            Log.d(TAG, "Starting document upload for URI: $uri")
            Log.d(TAG, "URI scheme: ${uri.scheme}, authority: ${uri.authority}")

            // Convert URI to File
            val file = createFileFromUri(uri, context, "document")
            if (file != null) {
                Log.d(TAG, "File created successfully: ${file.absolutePath}, size: ${file.length()} bytes")

                // Validate file before upload
                if (file.length() == 0L) {
                    Log.e(TAG, "File is empty")
                    callback(null)
                    cleanupFile(file)
                    return
                }

                // Upload using the companion object method
                uploadFile(
                    file = file,
                    onSuccess = { url ->
                        Log.d(TAG, "Document upload successful")
                        callback(url)
                        // Clean up temporary file
                        cleanupFile(file)
                    },
                    onError = { error ->
                        Log.e(TAG, "Document upload failed: $error")
                        callback(null)
                        // Clean up temporary file
                        cleanupFile(file)
                    }
                )
            } else {
                Log.e(TAG, "Failed to create file from URI")
                callback(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in uploadDocument", e)
            callback(null)
        }
    }

    private fun createFileFromUri(uri: Uri, context: Context, type: String): File? {
        return try {
            Log.d(TAG, "Creating file from URI: $uri")

            // Get file name and extension
            val fileName = getFileName(context, uri) ?: "temp_file"
            val extension = getFileExtension(context, uri, type)

            // Create unique file name
            val finalFileName = if (fileName.contains(".")) {
                fileName
            } else {
                "${fileName}.$extension"
            }

            val tempFile = File(context.cacheDir, "temp_${type}_${System.currentTimeMillis()}_$finalFileName")
            Log.d(TAG, "Creating temp file: ${tempFile.absolutePath}")

            // Copy content from URI to file
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    outputStream.flush()
                }
            }

            if (tempFile.exists() && tempFile.length() > 0) {
                Log.d(TAG, "Temp file created successfully, size: ${tempFile.length()} bytes")
                tempFile
            } else {
                Log.e(TAG, "Temp file creation failed or file is empty")
                cleanupFile(tempFile)
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating file from URI", e)
            null
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        return try {
            when (uri.scheme) {
                "content" -> {
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (displayNameIndex != -1) {
                                cursor.getString(displayNameIndex)
                            } else null
                        } else null
                    }
                }
                "file" -> {
                    File(uri.path ?: "").name
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file name", e)
            null
        }
    }

    private fun getFileExtension(context: Context, uri: Uri, type: String): String {
        return try {
            // First try to get from MIME type
            val mimeType = context.contentResolver.getType(uri)
            if (mimeType != null) {
                val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                if (extension != null) {
                    return extension
                }
            }

            // Fallback to URI path
            val path = uri.path
            if (path?.contains(".") == true) {
                return path.substringAfterLast(".")
            }

            // Default extensions based on type
            return when (type) {
                "image" -> "jpg"
                "document" -> "pdf"
                else -> "tmp"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file extension", e)
            when (type) {
                "image" -> "jpg"
                "document" -> "pdf"
                else -> "tmp"
            }
        }
    }

    private fun getImageExtension(context: Context, uri: Uri): String {
        return try {
            val mimeType = context.contentResolver.getType(uri)
            when (mimeType) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                "image/gif" -> "gif"
                "image/webp" -> "webp"
                else -> "jpg"
            }
        } catch (e: Exception) {
            "jpg"
        }
    }

    private fun getDocumentExtension(context: Context, uri: Uri): String {
        return try {
            val mimeType = context.contentResolver.getType(uri)
            when (mimeType) {
                "application/pdf" -> "pdf"
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                "image/jpg" -> "jpg"
                else -> {
                    // Try to get extension from URI path
                    val path = uri.path
                    if (path?.contains(".") == true) {
                        path.substringAfterLast(".")
                    } else {
                        "pdf"
                    }
                }
            }
        } catch (e: Exception) {
            "pdf"
        }
    }

    private fun cleanupFile(file: File?) {
        try {
            if (file?.exists() == true) {
                val deleted = file.delete()
                Log.d(TAG, "Cleanup file: ${file.name}, deleted: $deleted")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up file", e)
        }
    }
}