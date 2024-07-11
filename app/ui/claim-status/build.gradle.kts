plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.accompanist.pagerIndicators)
  implementation(libs.androidx.compose.foundationLayout)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.uiCore)
  implementation(libs.androidx.compose.uiUtil)
  implementation(libs.arrow.core)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
  implementation(projects.coreUiData)
}
