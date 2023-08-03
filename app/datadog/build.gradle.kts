plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.androidx.other.startup)
  implementation(libs.androidx.other.workManager)
  implementation(libs.datadog.sdk)
  implementation(libs.koin.android)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okhttp.core)
  implementation(libs.slimber)
  implementation(projects.authEventCore)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDatastorePublic)
}

android {
  lint {
    // Context: https://issuetracker.google.com/issues/265962219
    disable += "EnsureInitializerMetadata"
  }
}
