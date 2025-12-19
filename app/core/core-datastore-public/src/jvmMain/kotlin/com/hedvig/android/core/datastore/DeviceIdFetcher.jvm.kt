package com.hedvig.android.core.datastore

import kotlinx.coroutines.flow.firstOrNull

internal class JvmDeviceIdFetcher(
  private val deviceIdDataStore: DeviceIdDataStore
) : DeviceIdFetcher {
  override suspend fun fetch(): String? {
    return deviceIdDataStore.observeDeviceId().firstOrNull()
  }
}
