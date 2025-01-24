plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

hedvig {
  compose()
  serialization()
}

dependencies {
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.coil.coil)
  implementation(libs.coil.compose)
  implementation(libs.koin.compose)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.zoomable)
  implementation(projects.composeUi)
  implementation(projects.designSystemHedvig)
  implementation(projects.navigationCommon)
  implementation(projects.navigationCompose)
  implementation(projects.navigationCore)
}
