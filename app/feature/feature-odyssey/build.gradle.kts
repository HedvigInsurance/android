plugins {
  id("hedvig.android.feature")
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.serialization)
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material3.windowSizeClass)
  implementation(libs.androidx.compose.materialIconsExtended)
  implementation(libs.androidx.compose.uiCore)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.navigation.common)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.navigation.runtime)
  implementation(libs.arrow.core)
  implementation(libs.coil.coil)
  implementation(libs.coil.compose)
  implementation(libs.koin.android)
  implementation(libs.koin.compose)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.audioPlayerData)
  implementation(projects.audioPlayerUi)
  implementation(projects.composePhotoCaptureState)
  implementation(projects.composeUi)
  implementation(projects.coreCommonAndroidPublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDatastorePublic)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreFileUpload)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
  implementation(projects.coreUiData)
  implementation(projects.dataClaimFlow)
  implementation(projects.dataClaimTriaging)
  implementation(projects.designSystemHedvig)
  implementation(projects.languageCore)
  implementation(projects.navigationCompose)
  implementation(projects.navigationComposeTyped)
  implementation(projects.navigationCore)
  implementation(projects.notificationPermission)
  implementation(projects.uiEmergency)

  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.turbine)
  testImplementation(projects.coreCommonTest)
  testImplementation(projects.languageTest)
  testImplementation(projects.loggingTest)
}
