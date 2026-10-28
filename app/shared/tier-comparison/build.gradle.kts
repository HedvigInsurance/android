plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
  serialization()
  compose()
  viewModels()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.apollo.runtime)
      implementation(libs.arrow.core)
      implementation(libs.coroutines.core)
      implementation(libs.jetbrains.compose.ui.tooling.preview)
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.composeUi)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreResources)
      implementation(projects.designSystemHedvig)
      implementation(projects.featureFlags)
      implementation(projects.languageCore)
      implementation(projects.moleculePublic)
    }
  }
}
