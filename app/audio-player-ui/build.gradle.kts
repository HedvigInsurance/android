plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.jetbrains.compose.animation)
      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.compose.ui.tooling.preview)
      implementation(libs.jetbrains.compose.ui.util)
      implementation(libs.jetbrains.lifecycle.runtime)
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(projects.audioPlayerData)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreResources)
      implementation(projects.designSystemHedvig)
    }
  }
}
