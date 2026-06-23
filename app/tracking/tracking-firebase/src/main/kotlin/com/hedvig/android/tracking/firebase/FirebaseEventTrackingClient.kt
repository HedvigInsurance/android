package com.hedvig.android.tracking.firebase

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.tracking.EventTrackingClient
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class FirebaseEventTrackingClient(
  applicationContext: Context,
) : EventTrackingClient {
  @SuppressLint("MissingPermission")
  private val firebaseAnalytics = FirebaseAnalytics.getInstance(applicationContext)

  override fun setCollectionEnabled(enabled: Boolean) {
    firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
  }

  override fun trackEvent(name: String, parameters: Map<String, Any?>) {
    firebaseAnalytics.logEvent(name, parameters.toBundle())
  }

  override fun trackScreen(name: String, parameters: Map<String, Any?>) {
    val bundle = parameters.toBundle()
    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, name)
    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
  }

  override fun setUserId(userId: String?) {
    firebaseAnalytics.setUserId(userId)
  }

  override fun setUserProperty(name: String, value: String?) {
    firebaseAnalytics.setUserProperty(name, value)
  }
}

// Not androidx's bundleOf: Firebase Analytics only accepts String/Long/Double params, so we coerce into those
// (Int->Long, Float->Double, everything else->String) rather than preserve types and throw on the unexpected.
private fun Map<String, Any?>.toBundle(): Bundle {
  val bundle = Bundle()
  for ((key, value) in this) {
    when (value) {
      null -> Unit
      is String -> bundle.putString(key, value)
      is Int -> bundle.putLong(key, value.toLong())
      is Long -> bundle.putLong(key, value)
      is Float -> bundle.putDouble(key, value.toDouble())
      is Double -> bundle.putDouble(key, value)
      is Boolean -> bundle.putString(key, value.toString())
      else -> bundle.putString(key, value.toString())
    }
  }
  return bundle
}
