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
}

enum class Flavor {
  Production,
  Staging,
  Develop,
}
