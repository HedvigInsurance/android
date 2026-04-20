plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
  androidResources()
  serialization()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.jetbrains.components.resources)
      implementation(libs.jetbrains.compose.runtime)
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.coreResources)
    }
  }
}
