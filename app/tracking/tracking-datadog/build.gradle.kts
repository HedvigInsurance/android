plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(projects.trackingCore)
  implementation(libs.datadog.sdk.rum)
  implementation(libs.koin.core)
  implementation(projects.initializable)
}
