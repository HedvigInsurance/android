plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
  serialization()
}

dependencies {
  api(libs.androidx.compose.runtime)
  api(libs.androidx.compose.uiCore)

  implementation(libs.androidx.compose.animation)
  implementation(libs.androidx.compose.animationCore)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.foundationLayout)
  implementation(libs.androidx.compose.runtimeSaveable)
  implementation(libs.androidx.compose.uiGraphics)
  implementation(libs.androidx.compose.uiText)
  implementation(libs.androidx.compose.uiUnit)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.composeUi)
  implementation(projects.coreResources)
  implementation(projects.coreUiData)
  implementation(projects.dataContractAndroid)
  implementation(projects.dataContractPublic)
  implementation(projects.dataProductVariantPublic)
  implementation(projects.designSystemHedvig)
}
