plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(projects.apolloCore)
  implementation(projects.apolloGiraffePublic)
  implementation(projects.coreCommonAndroidPublic)
  implementation(projects.hanalyticsFeatureFlagsPublic)
  implementation(projects.languageCore)

  implementation(libs.androidx.datastore.core)
  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.arrow.core)
  implementation(libs.koin.core)
  implementation(libs.slimber)

  testImplementation(projects.hanalyticsFeatureFlagsTest)

  testImplementation(libs.junit)
  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.turbine)
}

android {
  namespace = "com.hedvig.android.notification.badge.data"
}
