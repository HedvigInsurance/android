plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
}

dependencies {
  implementation(libs.androidx.compose.materialIconsCore)
  implementation(libs.androidx.compose.uiCore)
}
