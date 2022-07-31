plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
  id("kotlin-parcelize")
}

dependencies {
  implementation(projects.coreCommon)
  implementation(projects.coreDatastore)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
  implementation(projects.hanalytics)

  implementation(libs.androidx.other.activityCompose)
  implementation(libs.androidx.other.appCompat)
  implementation(libs.koin.android)
  implementation(libs.shake)
  implementation(libs.slimber)
}
