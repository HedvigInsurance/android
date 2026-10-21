package com.hedvig.android.core.tracking.test

import com.hedvig.android.core.tracking.EventTrackingClient

class FakeEventTrackingClient : EventTrackingClient {
  data class TrackedEvent(val name: String, val parameters: Map<String, Any?>)

  val events = mutableListOf<TrackedEvent>()
  val userProperties = mutableMapOf<String, String?>()
  var lastUserId: String? = null
  var lastCollectionEnabled: Boolean = true

  override fun setCollectionEnabled(enabled: Boolean) {
    lastCollectionEnabled = enabled
  }

  override fun trackEvent(name: String, parameters: Map<String, Any?>) {
    events += TrackedEvent(name, parameters)
  }

  override fun trackScreen(name: String, screenClass: String?, parameters: Map<String, Any?>) {}

  override fun setUserId(userId: String?) {
    lastUserId = userId
  }

  override fun setUserProperty(name: String, value: String?) {
    userProperties[name] = value
  }
}
