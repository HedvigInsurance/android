plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  serialization()
  compose()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.jetbrains.navigation.compose)
      implementation(libs.jetbrains.compose.runtime)
      implementation(libs.jetbrains.lifecycle.viewmodel)
      implementation(libs.koin.composeViewModel)
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCompose)
    }
  }
}
