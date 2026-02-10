plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
  serialization()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.jetbrains.compose.runtime)
      api(libs.jetbrains.compose.ui)
      implementation(libs.jetbrains.compose.animation)
      implementation(libs.jetbrains.compose.animation.core)
      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.compose.foundation.layout)
      implementation(libs.jetbrains.compose.runtime.saveable)
      implementation(libs.jetbrains.compose.ui.graphics)
      implementation(libs.jetbrains.compose.ui.text)
      implementation(libs.jetbrains.compose.ui.unit)
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.composeUi)
      implementation(projects.coreResources)
      implementation(projects.coreUiData)
      implementation(projects.dataContract)
      implementation(projects.dataProductVariantPublic)
      implementation(projects.designSystemHedvig)
    }
  }
}
