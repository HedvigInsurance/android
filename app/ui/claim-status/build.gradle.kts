plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.androidx.compose.foundationLayout)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.uiCore)
  implementation(libs.androidx.compose.uiUtil)
  implementation(libs.arrow.core)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.composePagerIndicator)
  implementation(projects.coreResources)
  implementation(projects.coreUiData)
  implementation(projects.designSystemHedvig)
}
