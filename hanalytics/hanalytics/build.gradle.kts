@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.serialization)
}

dependencies {
  implementation(projects.coreCommon)
  implementation(projects.coreCommonAndroid)
  implementation(projects.coreDatastore)

  api(libs.hAnalytics)
  implementation(libs.androidx.lifecycle.common)
  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
  implementation(libs.okhttp.core)
  implementation(libs.serialization)
  implementation(libs.slimber)
}
