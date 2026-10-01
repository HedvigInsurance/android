plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
  serialization()
  compose()
  navKeys()
  viewModels()
}

dependencies {
  implementation(libs.apollo.normalizedCache)
  implementation(libs.apollo.runtime)
  implementation(libs.arrow.core)
  implementation(projects.apolloCore)
  implementation(projects.apolloNetworkCacheManager)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreBuildConstants)
  implementation(libs.jetbrains.compose.runtime)
  implementation(libs.jetbrains.lifecycle.runtime.compose)
  implementation(projects.composeUi)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreResources)
  implementation(projects.dataPayingMember)
  implementation(projects.designSystemHedvig)
  implementation(projects.featurePayinAccountNavigation)
  implementation(projects.moleculePublic)
  implementation(projects.navigationCommon)
  implementation(projects.navigationCompose)
  implementation(projects.navigationCore)
  implementation(libs.zXing)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.serialization.json)
  testImplementation(libs.robolectric)
  testImplementation(libs.turbine)
  testImplementation(projects.loggingTest)
  testImplementation(projects.moleculeTest)
}
