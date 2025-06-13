plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

dependencies {
  implementation(libs.androidx.compose.materialIconsCore)
  implementation(libs.androidx.compose.uiCore)
}
