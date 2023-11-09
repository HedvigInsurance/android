package com.hedvig.android.datadog.core.attributestracking

import com.datadog.android.Datadog
import com.datadog.android.rum.GlobalRumMonitor

interface DatadogAttributesManager {
  fun storeAttribute(key: String, value: Any?)

  fun deleteAttribute(key: String)
}

internal class DatadogAttributesManagerImpl : DatadogAttributesManager {
  override fun storeAttribute(key: String, value: Any?) {
    val sdkCore = Datadog.getInstance()
    sdkCore.addUserProperties(mapOf(key to value))
    GlobalRumMonitor.get(sdkCore).addAttribute(key, value)
  }

  override fun deleteAttribute(key: String) {
    val sdkCore = Datadog.getInstance()
    sdkCore.addUserProperties(mapOf(key to null))
    GlobalRumMonitor.get(sdkCore).removeAttribute(key)
  }
}
