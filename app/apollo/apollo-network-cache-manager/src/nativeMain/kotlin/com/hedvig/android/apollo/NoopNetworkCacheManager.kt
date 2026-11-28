package com.hedvig.android.apollo

import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

/** The iOS Apollo clients keep no normalized cache, so there is nothing to clear. */
@ContributesBinding(AppScope::class)
@Inject
internal class NoopNetworkCacheManager : NetworkCacheManager {
  override suspend fun clearCache() {}
}
