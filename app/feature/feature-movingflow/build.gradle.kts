plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

hedvig {
  apollo(packageName = "octopus.feature.movingflow")
  serialization()
  compose()
}
android {
  testOptions.unitTests.isReturnDefaultValues = true
}
dependencies {
  api(libs.androidx.navigation.common)

  implementation(libs.androidx.datastore.core)
  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.arrow.core)
  implementation(libs.coroutines.core)
  implementation(libs.koin.compose)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.composeUi)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreResources)
  implementation(projects.coreUiData)
  implementation(projects.dataContractAndroid)
  implementation(projects.dataProductVariantPublic)
  implementation(projects.designSystemHedvig)
  implementation(projects.moleculeAndroid)
  implementation(projects.moleculePublic)
  implementation(projects.navigationCommon)
  implementation(projects.navigationCompose)
  implementation(projects.tierComparison)
  implementation(projects.uiTiersAndAddons)
  implementation(libs.compose.richtext)
  implementation(libs.compose.richtextCommonmark)
  implementation(projects.featureFlagsPublic)

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
