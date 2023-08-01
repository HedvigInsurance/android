package com.hedvig.app.util.apollo

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.hedvig.android.apollo.NetworkCacheManager
import slimber.log.d
import slimber.log.e

internal class NetworkCacheManagerImpl(
  private val apolloClient: ApolloClient,
) : NetworkCacheManager {
  override fun clearCache() {
    val didClearAllRecords = apolloClient.apolloStore.clearAll()
    if (didClearAllRecords) {
      d { "Did clear entire apolloStore cache" }
    } else {
      e { "Failed to clear apolloStore cache" }
    }
  }
}
