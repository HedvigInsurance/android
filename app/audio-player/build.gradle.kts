plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
}

android {
  namespace = "com.hedvig.android.audio.player"
}

dependencies {
  implementation(projects.app.core.commonAndroid)
  implementation(projects.app.core.designSystem)
  implementation(projects.app.core.resources)

  implementation(libs.androidx.compose.animation)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.material)
  implementation(libs.androidx.compose.materialIconsExtended)
  implementation(libs.androidx.compose.uiUtil)
  implementation(libs.androidx.lifecycle.common)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.lifecycle.runtime)
  implementation(libs.slimber)

  testImplementation(libs.assertK)
  testImplementation(libs.junit)
}
