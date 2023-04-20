plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("kotlin-parcelize")
}

dependencies {
  implementation(projects.coreCommonAndroid)
  implementation(projects.coreDatastore)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
  implementation(projects.hanalytics.hanalyticsCore)
  implementation(projects.hanalytics.hanalyticsEngineeringApi)

  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.other.activityCompose)
  implementation(libs.androidx.other.appCompat)
  implementation(libs.koin.android)
  implementation(libs.slimber)
}

android {
  namespace = "com.hedvig.android.hanalytics.engineering"
}
