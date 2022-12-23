plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.auth.authAndroid)
  implementation(projects.coreCommonAndroid)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
  implementation(projects.hanalytics.hanalyticsCore)

  implementation(libs.androidx.other.appCompat)
  implementation(libs.androidx.other.activityCompose)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.material3.windowSizeClass)
  implementation(libs.koin.android)
}
