package com.hedvig.android.network.clients.di

import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.api.NormalizedCacheFactory
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.network.clients.ExtraKtorClientConfiguration
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
   * Unlike Android, there is no logout interceptor here. An `UNAUTHENTICATED` response is returned as an ordinary
   * error, because the iOS app owns the session and decides when to log out.
   */
  @Provides
  @SingleIn(AppScope::class)
  fun provideApolloClient(
    normalizedCacheFactory: NormalizedCacheFactory,
    httpClient: HttpClient,
    hedvigBuildConstants: HedvigBuildConstants,
  ): ApolloClient = buildHedvigApolloClient(normalizedCacheFactory, httpClient, hedvigBuildConstants, emptyList())
}

internal actual fun httpClientEngineFactory(): HttpClientEngineFactory<*> = Darwin
