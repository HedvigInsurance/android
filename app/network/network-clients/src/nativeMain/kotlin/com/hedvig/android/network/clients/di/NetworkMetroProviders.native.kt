package com.hedvig.android.network.clients.di

import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.api.NormalizedCacheFactory
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.network.clients.ExtraKtorClientConfiguration
import com.hedvig.android.network.clients.NetworkLogPolicy
import com.hedvig.android.network.clients.NoopExtraKtorClientConfiguration
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

@ContributesTo(AppScope::class)
interface NativeNetworkMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideExtraKtorClientConfiguration(): ExtraKtorClientConfiguration = NoopExtraKtorClientConfiguration()

  /**
   * The interceptor chain is logging only: an `UNAUTHENTICATED` response is an ordinary error, and the session is
   * owned by the iOS app.
   */
  @Provides
  @SingleIn(AppScope::class)
  fun provideApolloClient(
    normalizedCacheFactory: NormalizedCacheFactory,
    httpClient: HttpClient,
    hedvigBuildConstants: HedvigBuildConstants,
    networkLogPolicy: NetworkLogPolicy,
  ): ApolloClient = buildHedvigApolloClient(
    normalizedCacheFactory,
    httpClient,
    hedvigBuildConstants,
    emptyList(),
    networkLogPolicy,
  )
}

internal actual fun httpClientEngineFactory(): HttpClientEngineFactory<*> = Darwin
