package com.hedvig.android.core.buildconstants

interface AppBuildConfig {
  val debug: Boolean
  val applicationId: String
  val buildType: String
  val versionCode: Int
  val versionName: String
  val appFlavor: Flavor
  val osReleaseVersion: String
  val osSdkVersion: Int
  val brand: String
  val model: String
  val device: String
  val manufacturer: String

  /** The platform this build reports to the backend, in the `X-Platform` and `Hedvig-App-Version` headers. */
  val platformName: String
    get() = defaultPlatformName
}

enum class Flavor {
  Production,
  Staging,
  Develop,
}
