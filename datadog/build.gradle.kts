plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
}

dependencies {
  implementation(projects.coreCommon)

  implementation(libs.androidx.other.startup)
  implementation(libs.datadog.sdk)
  implementation(libs.koin.android)
  implementation(libs.okhttp.core)
  implementation(libs.slimber)
}
