plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
  alias(libs.plugins.composeJetbrainsCompilerGradlePlugin)
}

hedvig {
  compose()
}

kotlin {
  sourceSets {
    @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
    commonMain.dependencies {
      api(libs.androidx.lifecycle.viewModel)
      implementation(libs.coroutines.core)
      implementation(libs.molecule)
      implementation(libs.androidx.compose.runtime)
      implementation(libs.coroutines.core)
    }
  }
}
