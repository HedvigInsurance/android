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

  companion object {
    fun previewHedvigBuildConstants(): HedvigBuildConstants {
      return object : HedvigBuildConstants {
        override val urlGiraffeBaseApi: String = "urlBaseApi"
        override val urlGiraffeGraphql: String = "urlGraphql"
        override val urlGiraffeGraphqlSubscription: String = "urlGraphqlWs"
        override val urlGraphqlOctopus: String = "urlGraphqlOctopus"
        override val urlBaseWeb: String = "urlBaseWeb"
        override val urlHanalytics: String = "urlHanalytics"
        override val urlOdyssey: String = "urlOdyssey"
        override val appVersionName: String = "11.X.Y"
        override val appVersionCode: String = "42"
        override val appId: String = "com.hedvig.dev.app"
      }
    }
  }
}
