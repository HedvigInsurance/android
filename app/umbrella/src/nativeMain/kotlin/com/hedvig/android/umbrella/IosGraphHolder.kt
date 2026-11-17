package com.hedvig.android.umbrella

import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.datastore.DeviceIdFetcher
import com.hedvig.android.core.demomode.IosDemoMode
import com.hedvig.android.core.tracking.RumLogger
import com.hedvig.android.feature.claimhistory.ios.ClaimHistoryScreenModel
import com.hedvig.android.feature.claimhistory.ios.createClaimHistoryScreenModel
import com.hedvig.android.feature.payments.ios.PaymentDetailsScreenModel
import com.hedvig.android.feature.payments.ios.PaymentHistoryScreenModel
import com.hedvig.android.feature.payments.ios.PaymentManualChargeScreenModel
import com.hedvig.android.feature.payments.ios.PaymentOverviewScreenModel
import com.hedvig.android.feature.payments.ios.createPaymentDetailsScreenModel
import com.hedvig.android.feature.payments.ios.createPaymentHistoryScreenModel
import com.hedvig.android.feature.payments.ios.createPaymentManualChargeScreenModel
import com.hedvig.android.feature.payments.ios.createPaymentOverviewScreenModel
import com.hedvig.android.language.LanguageStorage
import com.hedvig.android.network.clients.AccessTokenFetcher
import dev.zacsweers.metro.createGraphFactory

/**
 * Builds the shared Kotlin graph and starts its initializables. Call it once per process. It reads [appBuildConfig] and
 * calls [LanguageStorage.getCurrentLanguageTag] on the calling thread, and makes no request. A Kotlin exception thrown
 * while building the graph is fatal once it reaches Swift.
 *
 * [accessTokenFetcher] is called before every request; when it throws, the request fails without being sent.
 *
 * Shared code reports actions and errors through [RumLogger], which throws until a logger is installed, so when none is
 * installed this installs [LogcatRumLogger] before building the graph.
 */
fun createIosGraphHolder(
  appBuildConfig: AppBuildConfig,
  accessTokenFetcher: AccessTokenFetcher,
  deviceIdFetcher: DeviceIdFetcher,
  languageStorage: LanguageStorage,
  demoMode: IosDemoMode,
): IosGraphHolder {
  if (!RumLogger.isInstalled) {
    RumLogger.install(LogcatRumLogger)
  }
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

  /** A new screen model with its own ViewModel. The caller owns it and must `close()` it. */
  fun paymentHistoryScreenModel(): PaymentHistoryScreenModel =
    createPaymentHistoryScreenModel(graph.metroViewModelFactory)

  /**
   * A new screen model with its own ViewModel, for the charge [chargeId]. Null and "" are the same: they select the
   * upcoming charge when the backend sent it without an id, otherwise the first past charge without one, otherwise
   * nothing, which shows Failed. The caller owns it and must `close()` it.
   */
  fun paymentDetailsScreenModel(chargeId: String?): PaymentDetailsScreenModel =
    createPaymentDetailsScreenModel(graph.metroViewModelFactory, chargeId)

  /** A new screen model with its own ViewModel. The caller owns it and must `close()` it. */
  fun paymentOverviewScreenModel(): PaymentOverviewScreenModel =
    createPaymentOverviewScreenModel(graph.metroViewModelFactory)

  /**
   * A new screen model with its own ViewModel, which charges the member only when its `triggerCharge()` is called. The
   * caller owns it and must `close()` it.
   */
  fun paymentManualChargeScreenModel(): PaymentManualChargeScreenModel =
    createPaymentManualChargeScreenModel(graph.metroViewModelFactory)

  /**
   * Tells Kotlin a session started ([loggedIn] true) or ended (false), so it clears what it cached for the previous
   * member. Callable from any thread; the clearing finishes asynchronously. No token passes through this call.
   */
  fun sessionChanged(loggedIn: Boolean) {
    if (loggedIn) {
      // The native auth event listeners do not read the tokens, which the event type requires.
      graph.authEventStorage.loggedIn(accessToken = "", refreshToken = "")
    } else {
      graph.authEventStorage.loggedOut()
    }
  }
}
