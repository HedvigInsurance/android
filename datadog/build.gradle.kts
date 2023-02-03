plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
}

dependencies {
  implementation(projects.apollo)
  implementation(projects.auth.authCore)
  implementation(projects.auth.authEvent)
  implementation(projects.coreCommon)

  implementation(libs.androidx.other.startup)
  implementation(libs.androidx.other.workManager)
  implementation(libs.datadog.sdk)
  implementation(libs.koin.android)
  implementation(libs.okhttp.core)
  implementation(libs.slimber)
}

android {
  lint {
    // Context: https://issuetracker.google.com/issues/265962219
    @Suppress("UnstableApiUsage")
    disable += "EnsureInitializerMetadata"
  }
}
