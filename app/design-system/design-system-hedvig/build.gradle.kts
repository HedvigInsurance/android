plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(libs.androidx.compose.foundation)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.foundationLayout)
  implementation(libs.androidx.compose.material3.windowSizeClass)
  implementation(libs.androidx.compose.materialRipple)
  implementation(libs.androidx.compose.uiGraphics)
  implementation(libs.androidx.graphicsShapes)
  implementation(libs.compose.richtext)
  implementation(libs.modal.sheet)
  implementation(projects.composeUi)
  implementation(projects.coreResources)
  implementation(projects.designSystemInternals)
  implementation(projects.navigationCore)
}
