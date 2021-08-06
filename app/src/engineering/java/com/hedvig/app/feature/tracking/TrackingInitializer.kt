package com.hedvig.app.feature.tracking

import android.content.Context
import androidx.startup.Initializer
import com.hedvig.app.feature.di.KoinInitializer
import i
import org.koin.core.context.loadKoinModules

class TrackingInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        i { "Initialized ${this.javaClass.name}" }
        loadKoinModules(trackingLogModule)
    }

    override fun dependencies() = listOf(KoinInitializer::class.java)
}
