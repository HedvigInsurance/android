plugins {
  id("hedvig.multiplatform.library.ios")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.moleculePublic)
      implementation(libs.coroutines.core)
      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.compose.runtime)
      implementation(libs.jetbrains.compose.ui)
      implementation(libs.jetbrains.compose.ui.tooling.preview)
      implementation(libs.jetbrains.lifecycle.runtime.compose)
    }
    jvmTest.dependencies {
      implementation(libs.assertK)
      implementation(libs.jetbrains.compose.ui.test)
      implementation(libs.junit)
      implementation(org.jetbrains.compose.ComposePlugin.DesktopDependencies.currentOs)
    }
  }
}
