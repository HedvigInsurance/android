plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(libs.androidx.other.startup)
  api(libs.okhttp.core)

  implementation(libs.datadog.sdk.core)
  implementation(libs.datadog.sdk.logs)
  implementation(libs.datadog.sdk.okhttp)
  implementation(libs.datadog.sdk.rum)
  implementation(libs.datadog.sdk.trace)
  implementation(libs.koin.core)
  implementation(libs.timber)
  implementation(projects.authCorePublic)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDatastorePublic)
  implementation(projects.initializable)
  implementation(projects.trackingCore)
  implementation(projects.trackingDatadog)
}

android {
  lint {
    // Context: https://issuetracker.google.com/issues/265962219
    disable += "EnsureInitializerMetadata"
  }
}
