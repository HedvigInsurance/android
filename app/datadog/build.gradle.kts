plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
}

dependencies {
  implementation(projects.authEventCore)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDatastorePublic)

  implementation(libs.androidx.other.startup)
  implementation(libs.androidx.other.workManager)
  implementation(libs.datadog.sdk)
  implementation(libs.koin.android)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okhttp.core)
  implementation(libs.slimber)
}

android {
  namespace = "com.hedvig.android.datadog"

  lint {
    // Context: https://issuetracker.google.com/issues/265962219
    disable += "EnsureInitializerMetadata"
  }
}
