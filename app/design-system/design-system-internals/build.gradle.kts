plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

hedvig {
  compose()
}

dependencies {
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.foundationLayout)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.materialRipple)
  implementation(libs.androidx.compose.uiGraphics)
  implementation(libs.androidx.graphicsShapes)
  implementation(projects.composeUi)
  implementation(projects.coreResources)
}
