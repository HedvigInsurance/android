plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

hedvig {
  compose()
}

dependencies {
  api(projects.placeholderCore)
  implementation(libs.androidx.compose.animationCore)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.uiUtil)
  implementation(projects.designSystemHedvig)
}
