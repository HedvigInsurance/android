package com.hedvig.android.network.clients.di

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.interceptor.ApolloInterceptor
import com.apollographql.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.cache.normalized.memory.MemoryCacheFactory
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.BaseHttpClient
import com.hedvig.android.core.common.di.PublicApolloClient
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.network.clients.NetworkLogPolicy
import com.hedvig.android.network.clients.apollo.LogoutOnUnauthenticatedInterceptor
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient

@ContributesTo(AppScope::class)
interface ApolloClientMetroProviders {
  /** The interceptor chain is [LogoutOnUnauthenticatedInterceptor], then logging. */
  @Provides
  @SingleIn(AppScope::class)
  fun provideApolloClient(
    authTokenService: AuthTokenService,
    demoManager: DemoManager,
    normalizedCacheFactory: NormalizedCacheFactory,
    httpClient: HttpClient,
    hedvigBuildConstants: HedvigBuildConstants,
    networkLogPolicy: NetworkLogPolicy,
  ): ApolloClient = buildHedvigApolloClient(
    normalizedCacheFactory,
    httpClient,
    hedvigBuildConstants,
    apolloPlatformInterceptors(authTokenService, demoManager),
    networkLogPolicy,
  )

  /**
   * Built on the token-free [BaseHttpClient], so a request never carries an `Authorization` header and never asks for a
   * token. It has its own small cache and no platform interceptors, so a rejected request cannot log the member out.
   */
  @Provides
  @SingleIn(AppScope::class)
  @PublicApolloClient
  fun providePublicApolloClient(
    @BaseHttpClient httpClient: HttpClient,
    hedvigBuildConstants: HedvigBuildConstants,
    networkLogPolicy: NetworkLogPolicy,
  ): ApolloClient = buildHedvigApolloClient(
    MemoryCacheFactory(maxSizeBytes = 1024 * 1024),
    httpClient,
    hedvigBuildConstants,
    platformInterceptors = emptyList(),
    networkLogPolicy = networkLogPolicy,
  )

  /** HTTP bodies and GraphQL data are logged outside production builds, GraphQL errors in every build. */
  @Provides
  fun provideNetworkLogPolicy(hedvigBuildConstants: HedvigBuildConstants): NetworkLogPolicy = NetworkLogPolicy(
    logsHttpBodies = !hedvigBuildConstants.isProduction,
    logsGraphqlData = !hedvigBuildConstants.isProduction,
    logsGraphqlErrors = true,
  )
}

/** A member whose session the backend rejects is logged out, unless in demo mode. */
internal fun apolloPlatformInterceptors(
  authTokenService: AuthTokenService,
  demoManager: DemoManager,
): List<ApolloInterceptor> = listOf(LogoutOnUnauthenticatedInterceptor(authTokenService, demoManager))
