package com.hedvig.android.umbrella

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.BaseHttpClient
import com.hedvig.android.core.common.di.PublicApolloClient
import com.hedvig.android.core.datastore.DeviceIdFetcher
import com.hedvig.android.core.datastore.di.NativeDataStoreMetroProviders
import com.hedvig.android.language.IosLanguageSource
import com.hedvig.android.network.clients.AccessTokenFetcher
import com.hedvig.android.network.clients.NetworkLogPolicy
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import io.ktor.client.HttpClient

/** What tests read from a graph: [IosGraph], or the test graph with a canned HTTP engine. */
internal interface IosGraphAccessors {
  val apolloClient: ApolloClient

  @PublicApolloClient
  val publicApolloClient: ApolloClient

  /** The member client's HTTP client, which adds the token. */
  val httpClient: HttpClient

  /** The token-free HTTP client the member's is built on. */
  @BaseHttpClient
  val baseHttpClient: HttpClient

  val networkLogPolicy: NetworkLogPolicy
}

/**
 * The iOS app's graph: the `AppScope` contributions of every module the framework links, with each platform input
 * coming from the iOS app through [Factory]. No read stores anything, so the DataStore the native datastore module
 * offers is left out.
 */
@DependencyGraph(AppScope::class, excludes = [NativeDataStoreMetroProviders::class])
internal interface IosGraph : IosGraphAccessors {
  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides appBuildConfig: AppBuildConfig,
      @Provides accessTokenFetcher: AccessTokenFetcher,
      @Provides deviceIdFetcher: DeviceIdFetcher,
      @Provides languageSource: IosLanguageSource,
      @Provides networkLogPolicy: NetworkLogPolicy,
    ): IosGraph
  }
}
