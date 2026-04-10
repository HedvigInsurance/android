plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  serialization()
  compose()
}

dependencies {
  implementation(libs.arrow.core)
  implementation(libs.jetbrains.compose.runtime)
  implementation(libs.jetbrains.lifecycle.runtime.compose)
  implementation(libs.jetbrains.navigation.compose)
  implementation(libs.koin.composeViewModel)
  implementation(libs.koin.core)
  implementation(projects.composeUi)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreResources)
  implementation(projects.designSystemHedvig)
  implementation(projects.moleculePublic)
  implementation(projects.navigationCommon)
  implementation(projects.navigationCompose)
  implementation(projects.navigationComposeTyped)
}
