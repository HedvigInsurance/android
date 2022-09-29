plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.apollo)
  implementation(projects.coreCommonAndroid)
  implementation(projects.coreDatastore)
  implementation(projects.hanalytics.hanalyticsFeatureFlags)
  implementation(projects.hedvigLanguage)

  implementation(libs.arrowKt.core)
  implementation(libs.koin.android)
  implementation(libs.slimber)

  testImplementation(projects.hanalytics.hanalyticsFeatureFlagsTest)

  testImplementation(libs.androidx.test.junit)
  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.turbine)
}
