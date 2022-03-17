package com.hedvig.app.feature.hanalytics

import android.content.Context
import androidx.startup.Initializer
import com.hedvig.app.feature.di.KoinInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ExperimentsInitializer : Initializer<Unit>, KoinComponent {
    private val experimentManager: ExperimentManager by inject()

    override fun create(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            experimentManager.preloadExperiments()
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf(KoinInitializer::class.java)
}
