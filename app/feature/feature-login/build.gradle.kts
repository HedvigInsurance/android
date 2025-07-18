plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  androidResources()
  compose()
  serialization()
}

android {
  testOptions.unitTests.isReturnDefaultValues = true
}

dependencies {
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.lifecycle.viewModel)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.koin.compose)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.zXing)
  implementation(projects.apolloCore)
  implementation(projects.authCorePublic)
  implementation(projects.authCoreTest)
  implementation(projects.composeUi)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDemoMode)
  implementation(projects.coreResources)
  implementation(projects.designSystemHedvig)
  implementation(projects.languageAndroid)
  implementation(projects.languageCore)
  implementation(projects.moleculeAndroid)
  implementation(projects.moleculePublic)
  implementation(projects.navigationCommon)
  implementation(projects.navigationCompose)
  implementation(projects.navigationCore)

  testImplementation(libs.androidx.datastore.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.robolectric)
  testImplementation(libs.turbine)
  testImplementation(projects.authCoreTest)
  testImplementation(projects.authEventCore)
  testImplementation(projects.authEventFake)
  testImplementation(projects.coreCommonTest)
  testImplementation(projects.coreDatastoreTest)
  testImplementation(projects.loggingTest)
  testImplementation(projects.moleculeTest)
}
