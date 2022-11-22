@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.serialization)
}

dependencies {
  implementation(libs.koin.android)
  implementation(libs.okhttp.core)
  implementation(libs.serialization.json)
  implementation(projects.coreCommon)
}
