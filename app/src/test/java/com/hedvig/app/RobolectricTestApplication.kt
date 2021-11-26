package com.hedvig.app

import androidx.startup.AppInitializer
import com.google.firebase.FirebaseApp
import com.hedvig.app.feature.di.KoinInitializer

class RobolectricTestApplication : TestApplication() {
    override fun onCreate() {
        FirebaseApp.initializeApp(this)
        AppInitializer.getInstance(this).initializeComponent(KoinInitializer::class.java)
        super.onCreate()
    }
}
