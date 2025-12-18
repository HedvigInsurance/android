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
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.activity.core)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.uiCore)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.lifecycle.runtime)
  implementation(libs.androidx.lifecycle.viewModel)
  implementation(libs.koin.android)
  implementation(libs.koin.core)
  implementation(projects.authCorePublic)
  implementation(projects.authlib)
  implementation(projects.designSystemHedvig)
  implementation(projects.moleculePublic)
  implementation(projects.navigationCore)

  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.turbine)
  testImplementation(projects.loggingTest)
  testImplementation(projects.moleculeTest)
}
