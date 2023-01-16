@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
  id("kotlin-parcelize")
  alias(libs.plugins.serialization)
}

dependencies {
  implementation(projects.auth.authAndroid)
  implementation(projects.auth.authCore)
  implementation(projects.coreCommon)
  implementation(projects.coreUi)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreNavigation)
  implementation(projects.hedvigLanguage)

  implementation(libs.androidx.other.activityCompose)
  implementation(libs.androidx.compose.material)
  implementation(libs.coil.coil)
  implementation(libs.datadog.sdk)
  implementation(libs.hedvig.odyssey)
  implementation(libs.koin.android)
  implementation(libs.timber)
}

android {
  namespace = "com.hedvig.android.odyssey"
}
