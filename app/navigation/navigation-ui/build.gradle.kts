plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.androidx.compose.materialIconsCore)
  implementation(libs.kiwi.navigationCompose)
  implementation(projects.coreIcons)
  implementation(projects.coreResources)
  implementation(projects.navigationCore)
}
