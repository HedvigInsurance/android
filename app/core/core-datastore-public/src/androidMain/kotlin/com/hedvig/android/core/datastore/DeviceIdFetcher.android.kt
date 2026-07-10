package com.hedvig.android.core.datastore

import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.firstOrNull

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class AndroidDeviceIdFetcher(
  private val deviceIdDataStore: DeviceIdDataStore,
) : DeviceIdFetcher {
  override suspend fun fetch(): String? {
    return deviceIdDataStore.observeDeviceId().firstOrNull()
  }
}
