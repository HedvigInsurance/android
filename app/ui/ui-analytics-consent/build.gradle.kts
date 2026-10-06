plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

android {
  testOptions.unitTests.isReturnDefaultValues = true
}

dependencies {
  api(projects.dataSettingsDatastorePublic)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.coroutines.core)
  implementation(projects.coreResources)
  implementation(projects.designSystemHedvig)

  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.turbine)
  testImplementation(projects.loggingTest)
  testImplementation(projects.moleculeTest)
  testImplementation(projects.theme)
}
