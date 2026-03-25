plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
  serialization()
  compose()
}

dependencies {
  api(libs.androidx.navigation.common)

  implementation(libs.androidx.navigation.compose)
  implementation(libs.apollo.runtime)
  implementation(libs.arrow.core)
  implementation(libs.coroutines.core)
  implementation(libs.jetbrains.lifecycle.runtime.compose)
  implementation(libs.koin.composeViewModel)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.composeUi)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreResources)
  implementation(projects.coreUiData)
  implementation(projects.designSystemHedvig)
  implementation(projects.moleculePublic)
  implementation(projects.navigationCommon)
  implementation(projects.navigationCompose)
  implementation(projects.navigationComposeTyped)
}
