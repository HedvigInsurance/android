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
      api(libs.jetbrains.lifecycle.viewmodel)
      api(libs.molecule)
      implementation(libs.coroutines.core)
      implementation(libs.jetbrains.compose.runtime)
      implementation(libs.coroutines.core)
    }
  }
}
