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
      implementation(libs.androidx.compose.animation)
      implementation(libs.androidx.compose.animationCore)
      implementation(libs.androidx.compose.foundation)
      implementation(libs.androidx.compose.foundationLayout)
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
