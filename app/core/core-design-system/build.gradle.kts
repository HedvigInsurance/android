plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(libs.androidx.compose.foundation)
  api(libs.androidx.compose.material3)

  implementation(libs.androidx.annotation)
  implementation(libs.androidx.graphicsShapes)
  implementation(libs.kotlinx.datetime)
  implementation(projects.coreIcons)
  implementation(projects.coreResources)
}

android {
  defaultConfig {
    vectorDrawables.useSupportLibrary = true
  }
}
