hedvig {
  apollo("octopus")
  serialization()
  compose()
}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

dependencies {
  api(libs.androidx.navigation.common)

  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.apollo.normalizedCache)
  implementation(libs.apollo.runtime)
  implementation(libs.arrow.core)
  implementation(libs.arrow.fx)
  implementation(libs.coroutines.core)
  implementation(libs.koin.compose)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreCommonAndroidPublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreFileUpload)
  implementation(projects.coreIcons)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
  implementation(projects.dataContractPublic)
  implementation(projects.designSystemHedvig)
  implementation(projects.languageCore)
  implementation(projects.moleculeAndroid)
  implementation(projects.moleculePublic)
  implementation(projects.navigationActivity)
  implementation(projects.navigationCompose)
  implementation(projects.navigationComposeTyped)
  implementation(projects.navigationCore)

  testImplementation(libs.apollo.testingSupport)
  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.testParameterInjector)
  testImplementation(projects.apolloOctopusTest)
  testImplementation(projects.apolloTest)
  testImplementation(projects.coreCommonTest)
  testImplementation(projects.featureFlagsTest)
  testImplementation(projects.loggingTest)
}
