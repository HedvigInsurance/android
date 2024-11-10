plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
}

dependencies {
  api(libs.androidx.compose.foundation)
  api(libs.androidx.compose.material3)

  implementation(libs.androidx.annotation)
  implementation(libs.androidx.graphicsShapes)
  implementation(libs.kotlinx.datetime)
  implementation(projects.composeUi)
  implementation(projects.coreIcons)
  implementation(projects.coreResources)
  implementation(projects.designSystemHedvig)
}

android {
  defaultConfig {
    vectorDrawables.useSupportLibrary = true
  }
}
