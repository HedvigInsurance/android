package com.hedvig.android.umbrella

import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.datastore.DeviceIdFetcher
import com.hedvig.android.core.demomode.IosDemoMode
import com.hedvig.android.feature.claimhistory.ios.ClaimHistoryScreenModel
import com.hedvig.android.feature.claimhistory.ios.createClaimHistoryScreenModel
import com.hedvig.android.language.LanguageStorage
import com.hedvig.android.network.clients.AccessTokenFetcher
import dev.zacsweers.metro.createGraphFactory

/**
 * Builds the shared Kotlin graph and starts its initializables. Call it once per process, on the main thread.
 *
 * [accessTokenFetcher] is called before every request; when it throws, the request fails without being sent.
 */
fun createIosGraphHolder(
  appBuildConfig: AppBuildConfig,
  accessTokenFetcher: AccessTokenFetcher,
  deviceIdFetcher: DeviceIdFetcher,
  languageStorage: LanguageStorage,
  demoMode: IosDemoMode,
): IosGraphHolder {
  val graph = createGraphFactory<IosGraph.Factory>().create(
    appBuildConfig = appBuildConfig,
    accessTokenFetcher = accessTokenFetcher,
    deviceIdFetcher = deviceIdFetcher,
    languageStorage = languageStorage,
    demoMode = demoMode,
  )
  graph.initializables.forEach { it.initialize() }
  return IosGraphHolder(graph)
}

class IosGraphHolder internal constructor(
  internal val graph: IosGraph,
) {
  /** A new screen model with its own ViewModel. The caller owns it and must `close()` it. */
  fun claimHistoryScreenModel(): ClaimHistoryScreenModel = createClaimHistoryScreenModel(graph.metroViewModelFactory)

  /**
   * Tells Kotlin a session started ([tokens] non-null) or ended (null), so it clears what it cached for the previous
   * member. Callable from any thread; the clearing finishes asynchronously. It never stores [tokens].
   */
  fun sessionChanged(tokens: SessionTokens?) {
    if (tokens == null) {
      graph.authEventStorage.loggedOut()
    } else {
      graph.authEventStorage.loggedIn(tokens.accessToken, tokens.refreshToken)
    }
  }
}

class SessionTokens(
  val accessToken: String,
  val refreshToken: String,
)
