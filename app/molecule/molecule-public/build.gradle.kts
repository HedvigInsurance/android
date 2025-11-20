plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.multiplatform.compose")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.androidx.lifecycle.viewModel)
      implementation(libs.coroutines.core)
      implementation(libs.molecule)
      implementation(libs.androidx.compose.runtime)
      implementation(libs.coroutines.core)
    }
  }
}
