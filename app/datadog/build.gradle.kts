plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
}

dependencies {
  implementation(projects.app.auth.authEventCore)
  implementation(projects.app.core.common)
  implementation(projects.app.core.datastore)

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
