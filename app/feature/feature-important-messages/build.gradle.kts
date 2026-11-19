plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
  viewModels()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.arrow.core)
      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.compose.runtime)
      implementation(libs.jetbrains.compose.ui.tooling.preview)
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(projects.coreCommonPublic)
      implementation(projects.dataImportantMessages)
      implementation(projects.designSystemHedvig)
      implementation(projects.moleculePublic)
    }
    jvmTest.dependencies {
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.jetbrains.compose.ui.test)
      implementation(libs.junit)
      implementation(libs.turbine)
      implementation(org.jetbrains.compose.ComposePlugin.DesktopDependencies.currentOs)
      implementation(projects.loggingTest)
      implementation(projects.moleculeTest)
    }
  }
}
