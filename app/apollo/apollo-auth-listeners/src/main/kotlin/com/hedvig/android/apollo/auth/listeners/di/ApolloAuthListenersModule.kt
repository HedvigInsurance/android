package com.hedvig.android.apollo.auth.listeners.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.auth.listeners.normalizedcache.ApolloNormalizedCacheAuthEventListener
import com.hedvig.android.apollo.auth.listeners.subscription.ApolloSubscriptionReconnectingAuthEventListener
import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.auth.event.AuthEventListener
import org.koin.dsl.bind
import org.koin.dsl.module

val apolloAuthListenersModule = module {
  single<ApolloNormalizedCacheAuthEventListener> {
    ApolloNormalizedCacheAuthEventListener(
      get<ApolloClient>(giraffeClient),
      get<ApolloClient>(octopusClient),
    )
  } bind AuthEventListener::class
  single<ApolloSubscriptionReconnectingAuthEventListener> {
    ApolloSubscriptionReconnectingAuthEventListener(
      get<ApolloClient>(giraffeClient),
    )
  } bind AuthEventListener::class
}
