package com.hedvig.android.apollo

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.apolloStore
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

interface NetworkCacheManager {
  fun clearCache()
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class ApolloNetworkCacheManager(
  private val apolloClient: ApolloClient,
) : NetworkCacheManager {
  override fun clearCache() {
    val didClearAllRecords = apolloClient.apolloStore.clearAll()
    if (didClearAllRecords) {
      logcat { "Did clear entire apolloStore cache" }
    } else {
      logcat(LogPriority.ERROR) { "Failed to clear apolloStore cache" }
    }
  }
}
