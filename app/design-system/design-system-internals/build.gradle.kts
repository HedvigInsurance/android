plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.compose.foundation.layout)
      implementation(libs.jetbrains.compose.material3)
      implementation(libs.jetbrains.compose.ui.graphics)
      implementation(projects.composeUi)
      implementation(projects.coreResources)
      implementation(projects.designSystemApi)
    }
  }
}
