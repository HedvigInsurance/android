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
   * staging    -> com.hedvig.app
   * develop    -> com.hedvig.dev.app
   *
   * Used to grab the right android:authorities="${applicationId}.provider" to picture access
   */
  val appPackageId: String

  /**
   * Returns the result of BuildConfig.DEBUG.
   */
  val isDebug: Boolean

  /**
   * Returns whether we are running in production environment (can be overridden at runtime in staging builds).
   * This is useful as [isDebug] only returns true for dev environment but false for staging environment and we
   * do sometimes want to know the difference.
   */
  val isProduction: Boolean

  /**
   * Returns whether this is a release build type (compile-time constant, cannot be overridden).
   * True for production release builds, false for debug and staging builds.
   * Use this to determine if environment switching should be allowed.
   */
  val isReleaseBuild: Boolean

  /**
   * The Android SDK version we are currently running on
   */
  val buildApiVersion: Int
}
