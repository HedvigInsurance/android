package com.hedvig.android.code.buildoconstants

interface HedvigBuildConstants {
  val urlGraphql: String
  val urlGraphqlWs: String
  val urlGraphqlOctopus: String
  val urlBaseApi: String

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
        override val urlGraphql: String = "urlGraphql"
        override val urlGraphqlWs: String = "urlGraphqlWs"
        override val urlGraphqlOctopus: String = "urlGraphqlOctopus"
        override val urlBaseApi: String = "urlBaseApi"
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
