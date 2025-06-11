plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

dependencies {
  api(libs.androidx.compose.foundation)
  api(libs.coil.coil)
  api(projects.designSystemApi)
  api(projects.placeholder)

  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.foundationLayout)
  implementation(libs.androidx.compose.material3.windowSizeClass)
  implementation(libs.androidx.compose.materialRipple)
  implementation(libs.androidx.compose.uiGraphics)
  implementation(libs.androidx.graphicsShapes)
  implementation(libs.androidx.other.core)
  implementation(libs.coil.compose)
  implementation(libs.compose.richtext)
  implementation(libs.modal.sheet)
  implementation(projects.composeUi)
  implementation(projects.coreResources)
  implementation(projects.coreUiData)
  implementation(projects.designSystemInternals)
  implementation(projects.navigationCore)
  implementation(libs.media3.exoplayer)
  implementation(libs.media3.exoplayer.dash)
  implementation(libs.media3.ui)
  implementation(libs.kotlinx.datetime)
}
