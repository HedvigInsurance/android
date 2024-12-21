plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

hedvig {
  compose()
}

dependencies {
  api(libs.androidx.compose.material3.windowSizeClass)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.foundationLayout)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.uiCore)
  implementation(projects.composeUi)
  implementation(projects.coreResources)
  implementation(projects.designSystemHedvig)
  implementation(projects.navigationCompose)
}
