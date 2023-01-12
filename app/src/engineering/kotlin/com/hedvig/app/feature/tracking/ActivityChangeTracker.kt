package com.hedvig.app.feature.tracking

import android.app.Activity
import android.app.Application
import android.os.Bundle
import slimber.log.d

class ActivityChangeTracker : Application.ActivityLifecycleCallbacks {
  override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    @Suppress("DEPRECATION")
    val activityName = activity.packageManager.getActivityInfo(activity.componentName, 0).name.split('.').last()
    d { "Activity:$activityName was created" }
  }

  override fun onActivityStarted(activity: Activity) {}
  override fun onActivityResumed(activity: Activity) {}
  override fun onActivityPaused(activity: Activity) {}
  override fun onActivityStopped(activity: Activity) {}
  override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
  override fun onActivityDestroyed(activity: Activity) {}
}
