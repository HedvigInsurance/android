package com.hedvig.android.shareddi

import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.datastore.DeviceIdFetcher
import com.hedvig.android.design.system.hedvig.IosDiHolder
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.language.LanguageStorage
import com.hedvig.android.network.clients.AccessTokenFetcher
import dev.zacsweers.metro.createGraphFactory

@Suppress("unused") // Used from iOS
fun initDiGraph(
  accessTokenFetcher: AccessTokenFetcher,
  deviceIdFetcher: DeviceIdFetcher,
  featureManager: FeatureManager,
  languageStorage: LanguageStorage,
  appBuildConfig: AppBuildConfig,
) {
  val graph = createGraphFactory<IosGraph.Factory>().create(
    accessTokenFetcher,
    deviceIdFetcher,
    featureManager,
    languageStorage,
    appBuildConfig,
  )
  IosDiHolder.metroViewModelFactory = graph.metroViewModelFactory
  IosDiHolder.imageLoader = graph.imageLoader
  IosDiHolder.graph = graph
}
