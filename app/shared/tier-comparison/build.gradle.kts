plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
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
  implementation(libs.apollo.runtime)
  implementation(libs.arrow.core)
  implementation(libs.coroutines.core)
  implementation(libs.jetbrains.lifecycle.runtime.compose)
  implementation(libs.koin.composeViewModel)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.composeUi)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreResources)
  implementation(projects.designSystemHedvig)
  implementation(projects.featureFlags)
  implementation(projects.languageCore)
  implementation(projects.moleculePublic)
}
