plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  api(libs.datadog.sdk.rum)
  api(projects.trackingCore)

  implementation(libs.koin.core)
  implementation(projects.initializable)
}
