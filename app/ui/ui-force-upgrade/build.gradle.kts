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
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(projects.coreResources)
      implementation(projects.designSystemHedvig)
    }
  }
}
