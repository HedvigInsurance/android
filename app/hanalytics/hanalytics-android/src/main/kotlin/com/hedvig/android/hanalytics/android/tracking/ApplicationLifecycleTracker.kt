package com.hedvig.android.hanalytics.android.tracking

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.hedvig.hanalytics.HAnalytics

class ApplicationLifecycleTracker(
  private val hAnalytics: HAnalytics,
  private val isProduction: Boolean,
) : DefaultLifecycleObserver {
  override fun onCreate(owner: LifecycleOwner) {
    if (isProduction) {
      hAnalytics.appStarted()
    }
  }

  override fun onStart(owner: LifecycleOwner) {
    if (isProduction) {
      hAnalytics.appResumed()
    }
  }

  override fun onStop(owner: LifecycleOwner) {
    if (isProduction) {
      hAnalytics.appBackground()
    }
  }
}
