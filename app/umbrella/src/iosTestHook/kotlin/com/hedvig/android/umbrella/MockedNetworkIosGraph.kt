package com.hedvig.android.umbrella

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.BaseHttpClient
import com.hedvig.android.core.common.di.PublicApolloClient
import com.hedvig.android.core.datastore.DeviceIdFetcher
import com.hedvig.android.core.datastore.di.NativeDataStoreMetroProviders
import com.hedvig.android.language.IosLanguageSource
import com.hedvig.android.language.LanguageService
import com.hedvig.android.network.clients.AccessTokenFetcher
import com.hedvig.android.network.clients.ExtraKtorClientConfiguration
import com.hedvig.android.network.clients.NetworkLogPolicy
import com.hedvig.android.network.clients.di.NativeNetworkMetroProviders
import com.hedvig.android.network.clients.di.NetworkMetroProviders
import com.hedvig.android.network.clients.di.buildKtorClient
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig

/**
 * [IosGraph]'s bindings, every `AppScope` contribution included, with one difference: the HTTP engine is [MockEngine]
 * instead of Darwin. The Ktor client is built by the app's own `buildKtorClient` and wrapped by the app's own token
 * plugin, and both Apollo clients by the app's own native providers, so every request reaches the iOS app's inputs
 * (token, device id, language) as on a device, and nothing reaches the network.
 */
@DependencyGraph(
  AppScope::class,
  excludes = [NativeNetworkMetroProviders::class, NetworkMetroProviders::class, NativeDataStoreMetroProviders::class],
)
internal interface MockedNetworkIosGraph : IosGraphAccessors {
  @Provides
  fun provideExtraKtorClientConfiguration(): ExtraKtorClientConfiguration = object : ExtraKtorClientConfiguration {
    override fun configure(config: HttpClientConfig<*>) {}
  }

  @Provides
  @SingleIn(AppScope::class)
  @BaseHttpClient
  fun provideBaseHttpClient(
    engine: MockEngine,
    hedvigBuildConstants: HedvigBuildConstants,
    languageService: LanguageService,
    deviceIdFetcher: DeviceIdFetcher,
    extraKtorClientConfiguration: ExtraKtorClientConfiguration,
    networkLogPolicy: NetworkLogPolicy,
  ): HttpClient = buildKtorClient(
    engineFactory = object : HttpClientEngineFactory<MockEngineConfig> {
      override fun create(block: MockEngineConfig.() -> Unit): HttpClientEngine = engine
    },
    hedvigBuildConstants = hedvigBuildConstants,
    languageService = languageService,
    deviceIdFetcher = deviceIdFetcher,
    extraKtorClientConfiguration = extraKtorClientConfiguration,
    networkLogPolicy = networkLogPolicy,
  )

  @Provides
  @SingleIn(AppScope::class)
  fun provideHttpClient(
    @BaseHttpClient baseHttpClient: HttpClient,
    accessTokenFetcher: AccessTokenFetcher,
  ): HttpClient = object : NetworkMetroProviders {}.provideHttpClient(baseHttpClient, accessTokenFetcher)

  @Provides
  @SingleIn(AppScope::class)
  fun provideApolloClient(
    httpClient: HttpClient,
    hedvigBuildConstants: HedvigBuildConstants,
    networkLogPolicy: NetworkLogPolicy,
  ): ApolloClient = object : NativeNetworkMetroProviders {}.provideApolloClient(
    httpClient = httpClient,
    hedvigBuildConstants = hedvigBuildConstants,
    networkLogPolicy = networkLogPolicy,
  )

  @Provides
  @SingleIn(AppScope::class)
  @PublicApolloClient
  fun providePublicApolloClient(
    @BaseHttpClient httpClient: HttpClient,
    hedvigBuildConstants: HedvigBuildConstants,
    networkLogPolicy: NetworkLogPolicy,
  ): ApolloClient = object : NativeNetworkMetroProviders {}.providePublicApolloClient(
    httpClient = httpClient,
    hedvigBuildConstants = hedvigBuildConstants,
    networkLogPolicy = networkLogPolicy,
  )

  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides appBuildConfig: AppBuildConfig,
      @Provides accessTokenFetcher: AccessTokenFetcher,
      @Provides deviceIdFetcher: DeviceIdFetcher,
      @Provides languageSource: IosLanguageSource,
      @Provides networkLogPolicy: NetworkLogPolicy,
      @Provides engine: MockEngine,
    ): MockedNetworkIosGraph
  }
}
