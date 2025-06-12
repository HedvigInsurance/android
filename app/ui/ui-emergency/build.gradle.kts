plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  serialization()
  compose()
}

dependencies {
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.foundationLayout)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.uiCore)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.coreResources)
  implementation(projects.coreUiData)
  implementation(projects.dataContractAndroid)
  implementation(projects.designSystemHedvig)
}
