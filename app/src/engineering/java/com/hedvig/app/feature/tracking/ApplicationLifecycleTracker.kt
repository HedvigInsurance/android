package com.hedvig.app.feature.tracking

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.hedvig.hanalytics.HAnalytics

class ApplicationLifecycleTracker(
    private val hAnalytics: HAnalytics,
) : DefaultLifecycleObserver {

    override fun onCreate(owner: LifecycleOwner) {
        hAnalytics.appStarted()
    }

    override fun onStart(owner: LifecycleOwner) {
        hAnalytics.appResumed()
    }

    override fun onStop(owner: LifecycleOwner) {
        hAnalytics.appBackground()
    }
}
