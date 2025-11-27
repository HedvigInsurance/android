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
      api(libs.coil.coil)
      api(libs.coil.compose)
      api(libs.coil.network.ktor)
      api(projects.designSystemApi)
      api(projects.placeholder)

      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.compose.foundation.layout)
      implementation(libs.jetbrains.compose.material.ripple)
      implementation(libs.jetbrains.compose.material3)
      implementation(libs.jetbrains.compose.material3.windowSizeClass)
      implementation(libs.jetbrains.compose.runtime)
      implementation(libs.jetbrains.compose.ui)
      implementation(libs.jetbrains.compose.ui.backhandler)
      implementation(libs.jetbrains.compose.ui.graphics)
      implementation(libs.jetbrains.compose.ui.tooling.preview)
      implementation(libs.jetbrains.navigationevent.compose)
      implementation(libs.androidx.graphicsShapes)
      implementation(projects.composeUi)
      implementation(projects.coreResources)
      implementation(projects.coreUiData)
      implementation(projects.designSystemInternals)
      implementation(projects.navigationCore)
      implementation(libs.kotlinx.datetime)
    }
    androidMain.dependencies {
      implementation(libs.androidx.other.core)
      implementation(libs.compose.richtext)
      implementation(libs.compose.richtextCommonmark)
      implementation(libs.media3.exoplayer)
      implementation(libs.media3.exoplayer.dash)
      implementation(libs.media3.ui)
    }
  }
}
