package com.hedvig.app.feature.tracking

import android.content.Context
import androidx.startup.Initializer
import com.hedvig.app.feature.di.KoinInitializer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.context.loadKoinModules

class TrackingInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        loadKoinModules(trackingLogModule)

        GlobalScope.launch {
            val shouldShowNotification = context
                .trackingPreferences
                .data
                .first()[SHOULD_SHOW_NOTIFICATION] ?: false
            runCatching { // Ignore if the service can't start due to launch restrictions.
                context.startService(TrackingShortcutService.newInstance(context, shouldShowNotification))
            }
        }
    }

    override fun dependencies() = listOf(KoinInitializer::class.java)
}
