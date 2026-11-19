package com.hedvig.android.network.clients.di

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.interceptor.ApolloInterceptor
import com.apollographql.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.cache.normalized.memory.MemoryCacheFactory
import com.apollographql.ktor.ktorClient
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.BaseHttpClient
import com.hedvig.android.core.common.di.PublicApolloClient
import com.hedvig.android.core.datastore.DeviceIdFetcher
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.logcat
import com.hedvig.android.network.clients.AccessTokenFetcher
import com.hedvig.android.network.clients.DeviceIdInterceptor
import com.hedvig.android.network.clients.ExtraKtorClientConfiguration
import com.hedvig.android.network.clients.HedvigHttpLogger
import com.hedvig.android.network.clients.NetworkLogPolicy
import com.hedvig.android.network.clients.apollo.addHedvigInterceptors
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpSendInterceptor
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.plugin
import io.ktor.client.request.header
import io.ktor.client.request.headers
import kotlin.time.Duration.Companion.seconds
import octopus.cache.Cache

@ContributesTo(AppScope::class)
interface NetworkMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  @BaseHttpClient
  fun provideBaseHttpClient(
    hedvigBuildConstants: HedvigBuildConstants,
    languageService: LanguageService,
    deviceIdFetcher: DeviceIdFetcher,
    extraKtorClientConfiguration: ExtraKtorClientConfiguration,
    networkLogPolicy: NetworkLogPolicy,
  ): HttpClient = buildKtorClient(
    httpClientEngineFactory(),
    hedvigBuildConstants,
    languageService,
    deviceIdFetcher,
    extraKtorClientConfiguration,
    networkLogPolicy,
  )

  @Provides
  @SingleIn(AppScope::class)
  fun provideHttpClient(
    @BaseHttpClient baseHttpClient: HttpClient,
    accessTokenFetcher: AccessTokenFetcher,
  ): HttpClient = baseHttpClient
    .config {}
    .apply { addAuthPlugin(accessTokenFetcher) }

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

  @Provides
  @SingleIn(AppScope::class)
  fun provideNormalizedCacheFactory(): NormalizedCacheFactory = MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024)
}

/**
 * The app's Apollo client: the normalized cache when [normalizedCacheFactory] is given, [httpClient] with its headers,
 * the Octopus URL, and the interceptor chain of [addHedvigInterceptors] with [platformInterceptors] ahead of logging.
 */
internal fun buildHedvigApolloClient(
  normalizedCacheFactory: NormalizedCacheFactory?,
  httpClient: HttpClient,
  hedvigBuildConstants: HedvigBuildConstants,
  platformInterceptors: List<ApolloInterceptor>,
  networkLogPolicy: NetworkLogPolicy,
): ApolloClient = ApolloClient.Builder()
  .apply { if (normalizedCacheFactory != null) with(Cache) { cache(normalizedCacheFactory) } }
  .ktorClient(httpClient)
  .httpServerUrl(hedvigBuildConstants.urlGraphqlOctopus)
  .addHedvigInterceptors(platformInterceptors, networkLogPolicy)
  .build()

internal fun buildKtorClient(
  engineFactory: HttpClientEngineFactory<*>,
  hedvigBuildConstants: HedvigBuildConstants,
  languageService: LanguageService,
  deviceIdFetcher: DeviceIdFetcher,
  extraKtorClientConfiguration: ExtraKtorClientConfiguration,
  networkLogPolicy: NetworkLogPolicy,
): HttpClient {
  return HttpClient(engineFactory) {
    installDatadogKtorPlugin(hedvigBuildConstants)
    install(HttpTimeout) {
      connectTimeoutMillis = CONNECT_TIMEOUT.inWholeMilliseconds
      socketTimeoutMillis = SOCKET_TIMEOUT.inWholeMilliseconds
    }
    defaultRequest {
      commonHeaders(hedvigBuildConstants, languageService)
    }
    if (networkLogPolicy.logsHttpBodies) {
      Logging {
        level = LogLevel.BODY
        logger = HedvigHttpLogger()
      }
    }
    install(HttpSend)
    extraKtorClientConfiguration.configure(this)
  }.apply {
    plugin(HttpSend).intercept(DeviceIdInterceptor(deviceIdFetcher))
  }
}

private fun DefaultRequest.DefaultRequestBuilder.commonHeaders(
  hedvigBuildConstants: HedvigBuildConstants,
  languageService: LanguageService,
) {
  headers {
    val languageTag = languageService.getLanguage().toBcp47Format()
    append("User-Agent", hedvigBuildConstants.userAgent)
    append("Accept-Language", languageTag)
    append("hedvig-language", languageTag)
    append("apollographql-client-name", platformClientName(hedvigBuildConstants.appPackageId))
    append("apollographql-client-version", hedvigBuildConstants.appVersionName)
    append("X-Build-Version", hedvigBuildConstants.appVersionCode)
    append("X-App-Version", hedvigBuildConstants.appVersionName)
    append("X-System-Version", hedvigBuildConstants.buildApiVersion.toString())
    append("X-Platform", hedvigBuildConstants.platformName)
    append("X-Model", hedvigBuildConstants.model)
    append(
      "Hedvig-App-Version",
      "${hedvigBuildConstants.platformName.lowercase()};${hedvigBuildConstants.appVersionName}",
    )
    appendPlatformHeaders()
  }
}

private fun HttpClient.addAuthPlugin(accessTokenFetcher: AccessTokenFetcher) {
  plugin(HttpSend).intercept { request ->
    val accessToken = accessTokenFetcher.fetch()
    execute(
      if (accessToken != null) {
        request.apply { header("Authorization", "Bearer ${accessTokenFetcher.fetch()}") }
      } else {
        request
      },
    )
  }
}

internal expect fun HttpClientConfig<*>.installDatadogKtorPlugin(hedvigBuildConstants: HedvigBuildConstants)

internal expect fun httpClientEngineFactory(): HttpClientEngineFactory<*>

private val CONNECT_TIMEOUT = 15.seconds

/**
 * Budget for a single stall, not for the whole call, so a large upload or PDF download is free to
 * take as long as it keeps making progress. Requests that need to tolerate longer stalls than a
 * cellular handover raise this per call via [io.ktor.client.plugins.timeout].
 */
private val SOCKET_TIMEOUT = 30.seconds
