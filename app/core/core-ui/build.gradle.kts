plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

hedvig {
  serialization()
  compose()
}

dependencies {
  api(libs.androidx.compose.foundation)
  api(libs.arrow.core)
  api(libs.coil.coil)
  api(projects.coreIcons)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material3.windowSizeClass)
  implementation(libs.androidx.compose.uiUtil)
  implementation(libs.androidx.other.core)
  implementation(libs.coil.compose)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.composeUi)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreResources)
  implementation(projects.coreUiData)
  implementation(projects.designSystemHedvig)
}
