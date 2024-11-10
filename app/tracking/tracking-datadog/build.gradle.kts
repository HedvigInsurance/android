hedvig {}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

dependencies {
  api(libs.datadog.sdk.rum)
  api(projects.trackingCore)

  implementation(libs.koin.core)
  implementation(projects.initializable)
}
