plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.app.apollo.core)
  implementation(projects.app.apollo.giraffe)
  implementation(projects.app.core.common)
  implementation(projects.app.hanalytics.hanalyticsFeatureFlags)
  implementation(projects.app.language.languageCore)

  implementation(libs.androidx.datastore.core)
  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.arrow.core)
  implementation(libs.koin.core)
  implementation(libs.slimber)

  testImplementation(projects.app.hanalytics.hanalyticsFeatureFlagsTest)

  testImplementation(libs.junit)
  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.turbine)
}

android {
  namespace = "com.hedvig.android.notification.badge.data"
}
