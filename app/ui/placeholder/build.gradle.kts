plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

dependencies {
  implementation(libs.androidx.compose.animationCore)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.uiUtil)
}
