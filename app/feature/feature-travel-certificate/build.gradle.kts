plugins {
  id("hedvig.android.feature")
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.serialization)
  alias(libs.plugins.squareSortDependencies)
  alias(libs.plugins.apollo)
}

dependencies {
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.navigation.common)
  implementation(libs.androidx.navigation.runtime)
  implementation(libs.arrow.core)
  implementation(libs.arrow.fx)
  implementation(libs.coroutines.core)
  implementation(libs.koin.android)
  implementation(libs.koin.compose)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.core)
  implementation(project(":data-contract-public"))
  apolloMetadata(projects.apolloOctopusPublic)
  implementation(libs.apollo.runtime)
  implementation(libs.apollo.normalizedCache)

  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreIcons)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
  implementation(projects.languageCore)
  implementation(projects.navigationComposeTyped)
  implementation(projects.navigationCore)
  implementation(projects.navigationActivity)
  implementation(projects.moleculeAndroid)
  implementation(projects.moleculePublic)
  implementation(projects.coreCommonAndroidPublic)
  implementation(projects.coreFileUpload)

  testImplementation(libs.apollo.testingSupport)
  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.testParameterInjector)
  testImplementation(libs.turbine)
  testImplementation(projects.apolloOctopusTest)
  testImplementation(projects.apolloTest)
  testImplementation(projects.coreCommonTest)
  testImplementation(projects.featureFlagsTest)
  testImplementation(projects.loggingTest)
}

apollo {
  service("octopus") {
    packageName.set("octopus")
  }
}
