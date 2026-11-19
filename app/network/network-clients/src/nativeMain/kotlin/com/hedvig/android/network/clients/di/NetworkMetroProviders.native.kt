package com.hedvig.android.network.clients.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.BaseHttpClient
import com.hedvig.android.core.common.di.PublicApolloClient
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
   * No normalized cache: every iOS read goes to the network and stores nothing, so one member's answer cannot reach
   * the next member, and a fetch policy that asks for the cache is answered from the network. The interceptor chain
   * is logging only: an `UNAUTHENTICATED` response is an ordinary error, and the session is owned by the iOS app.
   */
  @Provides
  @SingleIn(AppScope::class)
  fun provideApolloClient(
    httpClient: HttpClient,
    hedvigBuildConstants: HedvigBuildConstants,
    networkLogPolicy: NetworkLogPolicy,
  ): ApolloClient = buildHedvigApolloClient(
    null,
    httpClient,
    hedvigBuildConstants,
    emptyList(),
    networkLogPolicy,
  )

  /**
   * Built on the token-free [BaseHttpClient], so a request never carries an `Authorization` header and never asks the
   * iOS app for a token. No cache and no platform interceptors, as the member client.
   */
  @Provides
  @SingleIn(AppScope::class)
  @PublicApolloClient
  fun providePublicApolloClient(
    @BaseHttpClient httpClient: HttpClient,
    hedvigBuildConstants: HedvigBuildConstants,
    networkLogPolicy: NetworkLogPolicy,
  ): ApolloClient = buildHedvigApolloClient(
    null,
    httpClient,
    hedvigBuildConstants,
    emptyList(),
    networkLogPolicy,
  )
}

internal actual fun httpClientEngineFactory(): HttpClientEngineFactory<*> = Darwin
