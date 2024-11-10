hedvig {
}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
}

dependencies {
  api(libs.androidx.compose.foundation)
  api(libs.coil.coil)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.foundationLayout)
  implementation(libs.androidx.compose.material3.windowSizeClass)
  implementation(libs.androidx.compose.materialRipple)
  implementation(libs.androidx.compose.uiGraphics)
  implementation(libs.androidx.graphicsShapes)
  implementation(libs.coil.compose)
  implementation(libs.compose.richtext)
  implementation(libs.koin.core)
  implementation(libs.modal.sheet)
  implementation(projects.composeUi)
  implementation(projects.coreResources)
  implementation(projects.designSystemInternals)
  implementation(projects.navigationCore)
  implementation(projects.placeholder)
}
