plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

dependencies {
  implementation(libs.androidx.compose.foundation)
  implementation(libs.jetbrains.compose.runtime)
  implementation(libs.androidx.compose.uiCore)
  implementation(projects.designSystemHedvig)
}
