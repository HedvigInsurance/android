plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
  alias(libs.plugins.serialization)
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(libs.androidx.compose.foundation)
  api(libs.androidx.compose.material3)
  api(libs.arrow.core)
  api(projects.coreIcons)

  implementation(libs.androidx.compose.material3.windowSizeClass)
  implementation(libs.androidx.compose.uiUtil)
  api(libs.coil.coil)
  implementation(libs.coil.compose)
  implementation(libs.androidx.other.core)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.composeUi)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreResources)
  implementation(projects.coreUiData)
  implementation(projects.designSystemHedvig)
  implementation(projects.placeholder)
}
