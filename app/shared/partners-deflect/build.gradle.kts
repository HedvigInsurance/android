plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
  serialization()
}


kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.kotlinx.serialization.core)
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(libs.jetbrains.compose.animation)
      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.compose.ui)
      implementation(libs.jetbrains.compose.ui.tooling.preview)
      implementation(libs.jetbrains.navigationevent.compose)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreResources)
      implementation(projects.coreUiData)
      implementation(projects.designSystemHedvig)
      implementation(projects.languageCore)
      implementation(projects.composeUi)
    }
  }
}
