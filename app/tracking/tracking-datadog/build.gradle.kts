plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(libs.datadog.sdk.rum)
  api(projects.trackingCore)

  implementation(libs.koin.core)
  implementation(projects.initializable)
}
