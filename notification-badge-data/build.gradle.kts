plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.apollo)
  implementation(projects.coreCommon)
  implementation(projects.coreDatastore)
  implementation(projects.hanalytics)

  implementation(libs.koin.android)
  implementation(libs.slimber)

  testImplementation(projects.hanalyticsTest)

  testImplementation(libs.androidx.test.junit)
  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.turbine)
}
