package com.hedvig.android.datadog.core.attributestracking

import com.datadog.android.Datadog
import com.datadog.android.rum.GlobalRum

interface DatadogAttributesManager {
  fun storeAttribute(key: String, value: Any?)
  fun deleteAttribute(key: String)
}

internal class DatadogAttributesManagerImpl : DatadogAttributesManager {
  override fun storeAttribute(key: String, value: Any?) {
    Datadog.addUserExtraInfo(mapOf(key to value))
    GlobalRum.addAttribute(key, value)
  }

  override fun deleteAttribute(key: String) {
    Datadog.addUserExtraInfo(mapOf(key to null))
    GlobalRum.removeAttribute(key)
  }
}
