package com.hedvig.android.apollo.auth.listeners.subscription

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.auth.event.AuthEventListener

internal class ApolloSubscriptionReconnectingAuthEventListener(
  private val giraffeApolloClient: ApolloClient,
) : AuthEventListener {
  override suspend fun loggedIn(accessToken: String) {
    giraffeApolloClient.reconnectSubscriptions()
  }

  override suspend fun loggedOut() {
    giraffeApolloClient.reconnectSubscriptions()
  }
}
