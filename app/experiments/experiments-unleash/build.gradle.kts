plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  implementation(libs.coroutines.core)
  implementation(projects.coreCommonPublic)
  implementation(projects.experimentsPublic)
  implementation(projects.featureFlags)
  implementation(projects.initializable)
  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(projects.loggingTest)
}
