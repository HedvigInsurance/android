package com.hedvig.android.network.clients.di

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.interceptor.ApolloInterceptor
import com.apollographql.cache.normalized.api.NormalizedCacheFactory
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.di.AppScope
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

  /** HTTP bodies are logged outside production builds, GraphQL data in every build. */
  @Provides
  fun provideNetworkLogPolicy(hedvigBuildConstants: HedvigBuildConstants): NetworkLogPolicy = NetworkLogPolicy(
    logsHttpBodies = !hedvigBuildConstants.isProduction,
    logsGraphqlData = true,
  )
}

/** A member whose session the backend rejects is logged out, unless in demo mode. */
internal fun apolloPlatformInterceptors(
  authTokenService: AuthTokenService,
  demoManager: DemoManager,
): List<ApolloInterceptor> = listOf(LogoutOnUnauthenticatedInterceptor(authTokenService, demoManager))
