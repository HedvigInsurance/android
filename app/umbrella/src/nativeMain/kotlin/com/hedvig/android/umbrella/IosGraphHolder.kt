package com.hedvig.android.umbrella

import com.hedvig.android.core.buildconstants.Flavor
import com.hedvig.android.core.datastore.DeviceIdFetcher
import com.hedvig.android.core.tracking.RumLogger
import com.hedvig.android.iosdata.bridge.IosGraphHolder
import com.hedvig.android.iosdata.bridge.iosGraphHolder
import com.hedvig.android.language.IosLanguageSource
import com.hedvig.android.logger.IosLogcatLogger
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.network.clients.AccessTokenFetcher
import com.hedvig.android.network.clients.NetworkLogPolicy
import dev.zacsweers.metro.createGraphFactory
import kotlin.time.TimeSource

/**
 * The shared Kotlin graph for the iOS app, built on first use. Creating the holder only keeps the inputs: it builds
 * nothing, calls no input, installs no logger and sends no request, so it is safe to create at launch whether or not
 * anything uses it. Create one per process and keep it.
 *
 * The first call that needs the graph builds it on the calling thread and logs how long that took at INFO. Before
 * building, it installs an `NSLog` logger at WARN if the iOS app installed no log sink (see
 * `IosLogcatLogger.install(sink:minimumPriority:)`), and a [RumLogger] that writes to the log if none is installed.
 *
 * [appFlavor] decides the backend and what may be logged: a production flavor never logs HTTP bodies or GraphQL data,
 * whatever [networkLogPolicy] says. Everything else about the build comes from the app's bundle and the device.
 * [accessTokenFetcher], [deviceIdFetcher] and [languageSource] are called from any thread.
 */
fun createIosGraphHolder(
  appFlavor: Flavor,
  accessTokenFetcher: AccessTokenFetcher,
  deviceIdFetcher: DeviceIdFetcher,
  languageSource: IosLanguageSource,
  networkLogPolicy: NetworkLogPolicy,
): IosGraphHolder = iosGraphHolder {
  IosLogcatLogger.installFallback(LogPriority.WARN)
  if (!RumLogger.isInstalled) {
    RumLogger.install(LogcatRumLogger)
  }
  val start = TimeSource.Monotonic.markNow()
  createGraphFactory<IosGraph.Factory>().create(
    appBuildConfig = IosAppBuildConfig(appFlavor),
    accessTokenFetcher = accessTokenFetcher,
    deviceIdFetcher = deviceIdFetcher,
    languageSource = languageSource,
    networkLogPolicy = networkLogPolicy.allowedFor(appFlavor),
  ).also {
    logcat(LogPriority.INFO) { "Built the shared Kotlin graph in ${start.elapsedNow().inWholeMilliseconds} ms" }
  }
}

/** [this], or nothing logged beyond error text in a production build. */
internal fun NetworkLogPolicy.allowedFor(flavor: Flavor): NetworkLogPolicy = when (flavor) {
  Flavor.Production -> NetworkLogPolicy(
    logsHttpBodies = false,
    logsGraphqlData = false,
    logsGraphqlErrors = logsGraphqlErrors,
  )

  else -> this
}
