package com.hedvig.android.apollo.auth.listeners.normalizedcache

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.apolloStore
import com.hedvig.android.auth.event.AuthEventListener
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@ContributesIntoSet(AppScope::class)
@SingleIn(AppScope::class)
@Inject
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
