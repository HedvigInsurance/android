plugins {
  id("hedvig.android.feature")
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.serialization) //todo: maybe not needed at all - check!
  alias(libs.plugins.squareSortDependencies)
  id("kotlin-parcelize")
  alias(libs.plugins.apollo)
}

android {
  testOptions.unitTests.isReturnDefaultValues = true
}

dependencies {
  implementation(libs.apollo.normalizedCache)
  implementation(libs.apollo.runtime)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.navigation.common)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.arrow.core)
  implementation(libs.coil.coil)
  implementation(libs.coil.compose)
  implementation(libs.coil.gif)
  implementation(libs.coil.svg)
  implementation(libs.coroutines.core)
  implementation(libs.koin.android)
  implementation(libs.koin.compose)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreResources)
  implementation(projects.coreUiData)
  implementation(projects.dataContractAndroid)
  implementation(projects.dataContractPublic)
  implementation(projects.dataProductVariantAndroid)
  implementation(projects.dataProductVariantPublic)
  implementation(projects.designSystemHedvig)
  implementation(projects.languageCore)
  implementation(projects.moleculeAndroid)
  implementation(projects.moleculePublic)
  implementation(projects.navigationCompose)
  implementation(projects.navigationComposeTyped)
  implementation(projects.navigationCore)
  implementation(projects.dataChangetier)
  implementation(projects.featureFlagsPublic)
  implementation(projects.coreCommonAndroidPublic)
  implementation(projects.coreFileUpload)
  implementation(projects.composeUi)

  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.turbine)
  testImplementation(projects.coreCommonTest)
  testImplementation(projects.languageTest)
  testImplementation(projects.loggingTest)
  testImplementation(projects.moleculeTest)
}

apollo {
  service("octopus") {
    packageName = "octopus"
    dependsOn(projects.apolloOctopusPublic, true)
  }
}
