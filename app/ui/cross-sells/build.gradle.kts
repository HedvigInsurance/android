plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

dependencies {
  implementation(libs.androidx.compose.foundationLayout)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.uiCore)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.arrow.core)
  implementation(libs.coil.coil) //todo: or api?
  implementation(projects.placeholder)
  implementation(libs.coil.compose)
  implementation(projects.composeUi)
  implementation(projects.coreResources)
  implementation(projects.coreUiData)
  implementation(projects.dataAddons)
  implementation(projects.dataContractAndroid)
  implementation(projects.dataContractPublic)
  implementation(projects.designSystemHedvig)
}
