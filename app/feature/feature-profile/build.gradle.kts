plugins {
  id("hedvig.android.feature")
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.apollo)
  alias(libs.plugins.serialization)
  alias(libs.plugins.squareSortDependencies)
}

android {
  testOptions.unitTests.isReturnDefaultValues = true
}

dependencies {

  implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material3.windowSizeClass)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.apollo.normalizedCache)
  implementation(libs.apollo.runtime)
  implementation(libs.arrow.core)
  implementation(libs.kiwi.navigationCompose)
  implementation(libs.koin.android)
  implementation(libs.koin.compose)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.moneta)
  implementation(projects.apolloCore)
  implementation(projects.apolloNetworkCacheManager)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.authCorePublic)
  implementation(projects.composeUi)
  implementation(projects.composeWebview)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreCommonAndroidPublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDatastorePublic)
  implementation(projects.coreDemoMode)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreIcons)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
  implementation(projects.coreUiData)
  implementation(projects.dataSettingsDatastorePublic)
  implementation(projects.featureFlagsPublic)
  implementation(projects.languageCore)
  implementation(projects.languageData)
  implementation(projects.marketCore)
  implementation(projects.memberRemindersPublic)
  implementation(projects.memberRemindersUi)
  implementation(projects.moleculeAndroid)
  implementation(projects.moleculePublic)
  implementation(projects.navigationComposeTyped)
  implementation(projects.navigationCore)
  implementation(projects.notificationPermission)
  implementation(projects.placeholder)
  implementation(projects.pullrefresh)
  implementation(projects.theme)

  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.turbine)
  testImplementation(projects.coreCommonTest)
  testImplementation(projects.coreDatastoreTest)
  testImplementation(projects.dataSettingsDatastoreTest)
  testImplementation(projects.featureFlagsTest)
  testImplementation(projects.languageTest)
  testImplementation(projects.loggingTest)
  testImplementation(projects.marketTest)
  testImplementation(projects.memberRemindersTest)
  testImplementation(projects.moleculeTest)
}

apollo {
  service("octopus") {
    packageName.set("octopus")
    dependsOn(projects.apolloOctopusPublic, true)
  }
}
