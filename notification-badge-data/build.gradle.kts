plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.apollo.core)
  implementation(projects.apollo.giraffe)
  implementation(projects.coreCommon)
  implementation(projects.hanalytics.hanalyticsFeatureFlags)
  implementation(projects.hedvigLanguage)

  implementation(libs.androidx.datastore.core)
  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.arrow.core)
  implementation(libs.koin.core)
  implementation(libs.slimber)

  testImplementation(projects.hanalytics.hanalyticsFeatureFlagsTest)

  testImplementation(libs.junit)
  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.turbine)
}

android {
  namespace = "com.hedvig.android.notification.badge.data"
}
