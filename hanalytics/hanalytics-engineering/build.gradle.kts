plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
  id("kotlin-parcelize")
}

dependencies {
  implementation(projects.coreCommonAndroid)
  implementation(projects.coreDatastore)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
  implementation(projects.hanalytics.hanalytics)
  implementation(projects.hanalytics.hanalyticsEngineeringApi)

  implementation(libs.androidx.other.activityCompose)
  implementation(libs.androidx.other.appCompat)
  implementation(libs.koin.android)
  implementation(libs.shake)
  implementation(libs.slimber)
}
