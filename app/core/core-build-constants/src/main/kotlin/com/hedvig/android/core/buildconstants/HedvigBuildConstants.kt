package com.hedvig.android.core.buildconstants

interface HedvigBuildConstants {
  /**
   * Base URL for octupus backend
   */
  val urlGraphqlOctopus: String

  /**
   * Base URL for the hedvig website
   */
  val urlBaseWeb: String

  /**
   * The URL targeting odyssey backend
   */
  val urlOdyssey: String

  /**
   * The URL targeting bot service backend
   */
  val urlBotService: String

  /**
   * The URL targeting claims service backend
   */
  val urlClaimsService: String

  /**
   * The base hosts used for all deep links. It's a list to support the legacy firebase base link too
   */
  val deepLinkHosts: List<String>

  /**
   * e.g. 11.3.2
   */
  val appVersionName: String

  /**
   * e.g. 43 (debug builds) 5301 (app tester code)
   */
  val appVersionCode: String

  /**
   * The id/package name of the application.
   * production -> com.hedvig.app
   * staging    -> com.hedvig.test.app
   * develop    -> com.hedvig.dev.app
   */
  val appId: String

  /**
   * Returns the result of BuildConfig.DEBUG.
   */
  val isDebug: Boolean

  /**
   * Returns whether we are running in release mode.
   * This is useful as [isDebug] only returns true for dev environment but false for staging environment and we
   * do sometimes want to know the difference.
   */
  val isProduction: Boolean

  /**
   * The Android SDK version we are currently running on
   */
  val buildApiVersion: Int
}
