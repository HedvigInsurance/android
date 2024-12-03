package com.hedvig.android.datadog.core.attributestracking

import com.hedvig.android.core.datastore.DeviceIdDataStore
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

internal class DeviceIdProvider(
  private val deviceIdDataStore: DeviceIdDataStore,
) : DatadogAttributeProvider {
  override fun provide(): Flow<Pair<String, Any?>> {
    return deviceIdDataStore
      .observeDeviceId()
      .onEach { deviceId ->
        logcat(LogPriority.VERBOSE) { "Datadog stored device id attribute: $deviceId" }
      }
      .map { deviceId ->
        DEVICE_ID_KEY to deviceId
      }
  }

  companion object {
    private const val DEVICE_ID_KEY = "device_id"
  }
}
