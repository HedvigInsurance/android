plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  serialization()
  compose()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.compose.foundation.layout)
      implementation(libs.jetbrains.compose.runtime)
      implementation(libs.jetbrains.compose.ui)
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.coreResources)
      implementation(projects.coreUiData)
      implementation(projects.dataContract)
      implementation(projects.designSystemHedvig)
      implementation(projects.loggingPublic)
    }
  }
}
