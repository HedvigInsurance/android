package com.hedvig.android.apollo.auth.listeners.normalizedcache

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.hedvig.android.auth.event.AuthEventListener

internal class ApolloNormalizedCacheAuthEventListener(
  private val giraffeApolloClient: ApolloClient,
  private val octopusApolloClient: ApolloClient,
) : AuthEventListener {
  override suspend fun loggedIn(accessToken: String) {
    giraffeApolloClient.apolloStore.clearAll()
    octopusApolloClient.apolloStore.clearAll()
  }

  override suspend fun loggedOut() {
    giraffeApolloClient.apolloStore.clearAll()
    octopusApolloClient.apolloStore.clearAll()
  }
}
