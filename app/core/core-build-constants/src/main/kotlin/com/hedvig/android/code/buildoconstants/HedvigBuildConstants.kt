package com.hedvig.android.code.buildoconstants

interface HedvigBuildConstants {
  /**
   * Base URL for giraffe backend
   */
  val urlGiraffeBaseApi: String

  /**
   * Same as [urlGiraffeBaseApi] with /graphql suffix to it
   */
  val urlGiraffeGraphql: String

  /**
   * Same as [urlGiraffeBaseApi] but for subscriptions, which means it has `wss` instead of `https` and ends with a
   * `/subscriptions` suffix
   */
  val urlGiraffeGraphqlSubscription: String

  /**
   * Base URL for octupus backend
   */
  val urlGraphqlOctopus: String

  /**
   * Base URL for the hedvig website
   */
  val urlBaseWeb: String

  /**
   * The URL targeting Hanalytics backend
   */
  val urlHanalytics: String

  /**
   * The URL targeting odyssey backend
   */
  val urlOdyssey: String

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
}
