plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
  serialization()
  navKeys()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.coil.coil)
      implementation(libs.coil.compose)
      implementation(libs.jetbrains.compose.animation)
      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.compose.runtime)
      implementation(libs.jetbrains.compose.ui)
      implementation(libs.jetbrains.compose.ui.tooling.preview)
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(libs.kotlinx.serialization.core)
      implementation(libs.zoomable)
      implementation(projects.composeUi)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreResources)
      implementation(projects.designSystemHedvig)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCompose)
      implementation(projects.navigationCore)
    }
    androidMain.dependencies {
      implementation(libs.androidx.other.coreKtx)
    }
    jvmTest.dependencies {
      implementation(libs.junit)
      implementation(org.jetbrains.compose.ComposePlugin.DesktopDependencies.currentOs)
    }
  }
}
