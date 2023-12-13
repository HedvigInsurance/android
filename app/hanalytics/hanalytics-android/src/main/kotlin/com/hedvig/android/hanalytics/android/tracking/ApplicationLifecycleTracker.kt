package com.hedvig.android.hanalytics.android.tracking

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.hedvig.android.logger.logcat

class ApplicationLifecycleTracker() : DefaultLifecycleObserver {
  override fun onCreate(owner: LifecycleOwner) {
    logcat { "ApplicationLifecycleTracker: onCreate" }
  }

  override fun onStart(owner: LifecycleOwner) {
    logcat { "ApplicationLifecycleTracker: onStart" }
  }

  override fun onStop(owner: LifecycleOwner) {
    logcat { "ApplicationLifecycleTracker: onStop" }
  }
}
