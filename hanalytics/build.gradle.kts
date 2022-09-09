@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  id("kotlin-parcelize")
  alias(libs.plugins.serialization)
}

dependencies {
  implementation(projects.coreCommon)
  implementation(projects.coreDatastore)
  implementation(projects.coreResources)
  implementation(projects.hedvigMarket)

  api(libs.hAnalytics)
  implementation(libs.coroutines.core)
  implementation(libs.koin.android)
  implementation(libs.materialComponents)
  implementation(libs.okhttp.core)
  implementation(libs.serialization)
  implementation(libs.shake)
  implementation(libs.slimber)
}
