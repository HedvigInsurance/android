plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
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
  implementation(projects.coreResources)
  implementation(projects.coreUiData)
  implementation(projects.dataAddons)
  implementation(projects.dataContractAndroid)
  implementation(projects.dataContractPublic)
  implementation(projects.designSystemHedvig)
}
