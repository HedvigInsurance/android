plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  implementation(platform(libs.firebase.bom))
  implementation(libs.coroutines.core)
  implementation(libs.firebase.analytics)
  implementation(projects.authCorePublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDemoMode)
  implementation(projects.dataSettingsDatastorePublic)
  implementation(projects.initializable)
  implementation(projects.trackingCore)
  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.turbine)
  testImplementation(projects.loggingTest)
  testImplementation(projects.theme)
}
