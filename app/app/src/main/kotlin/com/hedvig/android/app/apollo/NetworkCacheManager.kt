package com.hedvig.android.app.apollo

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat

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
