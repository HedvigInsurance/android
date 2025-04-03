plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

hedvig {
  apollo("octopus")
  serialization()
  compose()
}

android {
  testOptions.unitTests.isReturnDefaultValues = true
}

dependencies {
  api(libs.androidx.navigation.common)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.arrow.core)
  implementation(libs.arrow.fx)
  implementation(libs.coroutines.core)
  implementation(libs.koin.compose)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.apolloCore)
  implementation(projects.apolloNetworkCacheManager)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreAppReview)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDemoMode)
  implementation(projects.coreResources)
  implementation(projects.coreUiData)
  implementation(projects.dataCrossSellAfterFlow)
  implementation(projects.designSystemHedvig)
  implementation(projects.moleculeAndroid)
  implementation(projects.moleculePublic)
  implementation(projects.navigationCommon)
  implementation(projects.navigationCompose)
  implementation(projects.navigationComposeTyped)
  implementation(projects.navigationCore)

  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.turbine)
  testImplementation(projects.apolloOctopusTest)
  testImplementation(projects.apolloTest)
  testImplementation(projects.coreCommonTest)
  testImplementation(projects.featureFlagsTest)
  testImplementation(projects.languageTest)
  testImplementation(projects.loggingTest)
  testImplementation(projects.memberRemindersTest)
  testImplementation(projects.moleculeTest)
  testImplementation(projects.testClock)
}
