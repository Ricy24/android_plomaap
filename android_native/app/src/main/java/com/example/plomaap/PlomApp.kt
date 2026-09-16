package com.example.plomaap

import android.app.Application
import android.content.pm.PackageManager
import com.google.android.libraries.places.api.Places

class PlomApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            val ai = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val apiKey = ai.metaData?.getString("com.google.android.geo.API_KEY") ?: BuildConfig.MAPS_API_KEY
            if (!apiKey.isNullOrEmpty() && !Places.isInitialized()) {
                Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
