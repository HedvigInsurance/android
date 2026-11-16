package com.hedvig.android.umbrella

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.auth.event.AuthEventListener
import com.hedvig.android.auth.event.AuthEventStorage
import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.datastore.DeviceIdFetcher
import com.hedvig.android.core.demomode.IosDemoMode
import com.hedvig.android.initializable.Initializable
import com.hedvig.android.language.LanguageStorage
import com.hedvig.android.network.clients.AccessTokenFetcher
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.viewmodel.ViewModelGraph

/**
 * The iOS app's single graph. There is no Activity, so [ActivityRetainedScope] shares the graph's lifetime through
 * [additionalScopes]. Every platform input comes from the iOS app through [Factory].
 */
@DependencyGraph(AppScope::class, additionalScopes = [ActivityRetainedScope::class])
internal interface IosGraph : ViewModelGraph {
  val initializables: Set<Initializable>

  val authEventStorage: AuthEventStorage

  val authEventListeners: Set<AuthEventListener>

  val apolloClient: ApolloClient

  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides appBuildConfig: AppBuildConfig,
      @Provides accessTokenFetcher: AccessTokenFetcher,
      @Provides deviceIdFetcher: DeviceIdFetcher,
      @Provides languageStorage: LanguageStorage,
      @Provides demoMode: IosDemoMode,
    ): IosGraph
  }
}
