plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
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
  implementation(projects.composeUi)
  implementation(projects.coreResources)
  implementation(projects.dataContractAndroid)
  implementation(projects.dataContractPublic)
  implementation(projects.dataProductVariantPublic)
  implementation(projects.designSystemHedvig)
}
