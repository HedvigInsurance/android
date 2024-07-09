plugins {
  id("hedvig.android.feature")
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("kotlin-parcelize")
  alias(libs.plugins.apollo)
  alias(libs.plugins.serialization)
  alias(libs.plugins.squareSortDependencies)
}

android {
  testOptions.unitTests.isReturnDefaultValues = true
}

dependencies {

  implementation(libs.accompanist.pagerIndicators)
  implementation(libs.accompanist.permissions)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.uiUtil)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.lifecycle.runtime)
  implementation(libs.androidx.navigation.common)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.apollo.normalizedCache)
  implementation(libs.apollo.runtime)
  implementation(libs.arrow.core)
  implementation(libs.arrow.fx)
  implementation(libs.coil.compose)
  implementation(libs.koin.compose)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.moneta)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.audioPlayerData)
  implementation(projects.audioPlayerUi)
  implementation(projects.claimStatus)
  implementation(projects.composeUi)
  implementation(projects.coreCommonAndroidPublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDemoMode)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreIcons)
  implementation(projects.coreMarkdown)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
  implementation(projects.crossSells)
  implementation(projects.dataChatReadTimestampPublic)
  implementation(projects.dataContractAndroid)
  implementation(projects.featureFlagsPublic)
  implementation(projects.languageCore)
  implementation(projects.marketCore)
  implementation(projects.memberRemindersPublic)
  implementation(projects.memberRemindersUi)
  implementation(projects.moleculeAndroid)
  implementation(projects.moleculePublic)
  implementation(projects.navigationActivity)
  implementation(projects.navigationComposeTyped)
  implementation(projects.navigationCore)
  implementation(projects.notificationBadgeDataPublic)
  implementation(projects.notificationPermission)
  implementation(projects.pullrefresh)
  implementation(projects.uiEmergency)

  testImplementation(libs.apollo.testingSupport)
  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.testParameterInjector)
  testImplementation(libs.turbine)
  testImplementation(projects.apolloOctopusTest)
  testImplementation(projects.apolloTest)
  testImplementation(projects.coreCommonTest)
  testImplementation(projects.dataChatReadTimestampTest)
  testImplementation(projects.featureFlagsTest)
  testImplementation(projects.languageTest)
  testImplementation(projects.loggingTest)
  testImplementation(projects.memberRemindersTest)
  testImplementation(projects.moleculeTest)
  testImplementation(projects.notificationBadgeDataFake)
  testImplementation(projects.testClock)
}

apollo {
  service("octopus") {
    packageName.set("octopus")
    generateDataBuilders.set(true)
    dependsOn(projects.apolloOctopusPublic)
  }
}
