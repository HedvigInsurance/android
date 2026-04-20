plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  serialization()
  compose()
}

dependencies {
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.foundationLayout)
  implementation(libs.jetbrains.compose.runtime)
  implementation(libs.jetbrains.compose.ui)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.coreResources)
  implementation(projects.coreUiData)
  implementation(projects.dataContract)
  implementation(projects.designSystemHedvig)
}
