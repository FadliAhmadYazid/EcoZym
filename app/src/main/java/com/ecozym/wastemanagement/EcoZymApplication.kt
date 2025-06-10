package com.ecozym.wastemanagement

import androidx.multidex.MultiDexApplication
import com.ecozym.wastemanagement.utils.CloudinaryHelper
import dagger.hilt.android.HiltAndroidApp
import com.google.firebase.FirebaseApp

@HiltAndroidApp
class EcoZymApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        // Initialize Cloudinary
        try {
            CloudinaryHelper.initCloudinary(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}