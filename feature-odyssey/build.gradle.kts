@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
  id("kotlin-parcelize")
  alias(libs.plugins.serialization)
}
dependencies {
  implementation(projects.apollo)
  implementation(projects.auth.authAndroid)
  implementation(projects.auth.authCore)
  implementation(projects.coreCommon)
  implementation(projects.coreUi)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreUi)
  implementation(projects.coreResources)
  implementation(projects.hedvigLanguage)
  implementation(projects.navigation.navigationActivity)
  implementation(projects.hedvigMarket)
  implementation(projects.apollo)

  implementation(libs.accompanist.navigationAnimation)
  implementation(libs.accompanist.permissions)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.material)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.other.activityCompose)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.other.activityCompose)
  implementation(libs.androidx.compose.material)
  implementation(libs.apollo.normalizedCache)
  implementation(libs.coil.coil)
  implementation(libs.coil.compose)
  implementation(libs.datadog.sdk)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.hedvig.odyssey)
  implementation(libs.hedvig.odyssey)
  implementation(libs.koin.android)
  implementation(libs.koin.android)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.slimber)
}

android {
  namespace = "com.hedvig.android.odyssey"
}
