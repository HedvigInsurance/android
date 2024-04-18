package com.hedvig.android.apollo

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat

interface NetworkCacheManager {
  fun clearCache()
}

internal class NetworkCacheManagerImpl(
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
