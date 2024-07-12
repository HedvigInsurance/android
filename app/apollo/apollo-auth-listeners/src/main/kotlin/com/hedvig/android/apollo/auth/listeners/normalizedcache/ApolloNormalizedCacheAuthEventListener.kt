package com.hedvig.android.apollo.auth.listeners.normalizedcache

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.apolloStore
import com.hedvig.android.auth.event.AuthEventListener

internal class ApolloNormalizedCacheAuthEventListener(
  private val apolloClient: ApolloClient,
) : AuthEventListener {
  override suspend fun loggedIn(accessToken: String) {
    apolloClient.apolloStore.clearAll()
  }

  override suspend fun loggedOut() {
    apolloClient.apolloStore.clearAll()
  }
}
