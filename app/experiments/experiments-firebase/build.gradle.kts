plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  implementation(platform(libs.firebase.bom))
  implementation(libs.coroutines.core)
  implementation(libs.firebase.config)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreCommonPublic)
  implementation(projects.experimentsPublic)
  implementation(projects.initializable)
  implementation(projects.trackingCore)
  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(projects.loggingTest)
}
