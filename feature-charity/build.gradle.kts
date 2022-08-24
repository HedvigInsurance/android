plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.apollo)
  implementation(projects.coreCommon)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
  implementation(projects.hanalytics)

  implementation(libs.androidx.other.appCompat)
  implementation(libs.androidx.other.activityCompose)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.accompanist.placeholder)
  implementation(libs.coil.compose)
  implementation(libs.koin.android)
}
