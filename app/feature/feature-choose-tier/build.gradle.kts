plugins {
  id("hedvig.android.feature")
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.serialization)
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
  alias(libs.plugins.apollo)
  id("kotlin-parcelize")
}

android {
  testOptions.unitTests.isReturnDefaultValues = true
}

dependencies {
  api(libs.androidx.navigation.common)

  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.apollo.runtime)
  implementation(libs.arrow.core)
  implementation(libs.coroutines.core)
  implementation(libs.koin.compose)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.composeUi)
  implementation(projects.coreCommonAndroidPublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreFileUpload)
  implementation(projects.coreResources)
  implementation(projects.coreUiData)
  implementation(projects.dataChangetier)
  implementation(projects.dataContractAndroid)
  implementation(projects.dataContractPublic)
  implementation(projects.dataProductVariantAndroid)
  implementation(projects.dataProductVariantPublic)
  implementation(projects.designSystemHedvig)
  implementation(projects.featureFlagsPublic)
  implementation(projects.languageCore)
  implementation(projects.moleculeAndroid)
  implementation(projects.moleculePublic)
  implementation(projects.navigationCompose)
  implementation(projects.navigationComposeTyped)
  implementation(projects.navigationCore)
  implementation(projects.uiTiersAndAddons)

  testImplementation(libs.apollo.testingSupport)
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
  testImplementation(projects.moleculeTest)
  testImplementation(projects.testClock)
}

apollo {
  service("octopus") {
    packageName = "octopus"
    dependsOn(projects.apolloOctopusPublic, true)
  }
}
