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
      implementation(libs.arrow.core)
      implementation(libs.coil.coil)
      implementation(libs.coil.compose)
      implementation(libs.jetbrains.compose.foundation.layout)
      implementation(libs.jetbrains.compose.runtime)
      implementation(libs.jetbrains.compose.ui)
      implementation(libs.jetbrains.compose.ui.tooling.preview)
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(projects.composeUi)
      implementation(projects.coreResources)
      implementation(projects.coreUiData)
      implementation(projects.dataAddons)
      implementation(projects.dataContract)
      implementation(projects.designSystemHedvig)
      //todo: or api?
      implementation(projects.placeholder)
    }
    jvmTest.dependencies {
      implementation(libs.assertK)
      implementation(libs.junit)
      implementation(projects.trackingTest)
    }
  }
}
