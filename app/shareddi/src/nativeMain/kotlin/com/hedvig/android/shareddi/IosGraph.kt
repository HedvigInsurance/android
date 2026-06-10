package com.hedvig.android.shareddi

import coil3.ImageLoader
import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.datastore.DeviceIdFetcher
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.language.LanguageStorage
import com.hedvig.android.network.clients.AccessTokenFetcher
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.viewmodel.ViewModelGraph

@DependencyGraph(AppScope::class)
internal interface IosGraph : ViewModelGraph {
  val imageLoader: ImageLoader

  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides accessTokenFetcher: AccessTokenFetcher,
      @Provides deviceIdFetcher: DeviceIdFetcher,
      @Provides featureManager: FeatureManager,
      @Provides languageStorage: LanguageStorage,
      @Provides appBuildConfig: AppBuildConfig,
    ): IosGraph
  }
}
