plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
  androidResources()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.coreResources)
      implementation(libs.jetbrains.components.resources)
      implementation(libs.jetbrains.compose.runtime)
    }
  }
}
