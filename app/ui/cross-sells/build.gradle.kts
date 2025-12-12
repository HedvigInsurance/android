plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

dependencies {
  implementation(libs.androidx.compose.foundationLayout)
  implementation(libs.jetbrains.compose.runtime)
  implementation(libs.jetbrains.compose.ui)
  implementation(libs.jetbrains.lifecycle.runtime.compose)
  implementation(libs.arrow.core)
  implementation(libs.coil.coil) //todo: or api?
  implementation(projects.placeholder)
  implementation(libs.coil.compose)
  implementation(projects.composeUi)
  implementation(projects.coreResources)
  implementation(projects.coreUiData)
  implementation(projects.dataAddons)
  implementation(projects.dataContract)
  implementation(projects.designSystemHedvig)
}
