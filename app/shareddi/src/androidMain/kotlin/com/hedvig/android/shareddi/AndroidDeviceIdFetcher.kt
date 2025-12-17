package com.hedvig.android.shareddi

import com.hedvig.android.core.datastore.DeviceIdDataStore
import kotlinx.coroutines.flow.firstOrNull

internal class AndroidDeviceIdFetcher(
  private val deviceIdDataStore: DeviceIdDataStore
) : DeviceIdFetcher {
  override suspend fun fetch(): String? {
    return deviceIdDataStore.observeDeviceId().firstOrNull()
  }
}
