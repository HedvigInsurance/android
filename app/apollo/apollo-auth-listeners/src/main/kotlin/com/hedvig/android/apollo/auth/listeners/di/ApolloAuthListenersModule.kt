package com.hedvig.android.apollo.auth.listeners.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.auth.listeners.normalizedcache.ApolloNormalizedCacheAuthEventListener
import com.hedvig.android.auth.event.AuthEventListener
import org.koin.dsl.bind
import org.koin.dsl.module

val apolloAuthListenersModule = module {
  single<ApolloNormalizedCacheAuthEventListener> {
    ApolloNormalizedCacheAuthEventListener(
      get<ApolloClient>(),
    )
  } bind AuthEventListener::class
}
