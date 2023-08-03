package com.hedvig.android.code.buildoconstants

interface HedvigBuildConstants {
  val urlGraphql: String
  val urlGraphqlWs: String
  val urlGraphqlOctopus: String
  val urlBaseApi: String
  val urlBaseWeb: String
  val urlHanalytics: String

  // The URL targeting odyssey backend
  val urlOdyssey: String

  val appVersionName: String

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
      }
    }
  }
}
