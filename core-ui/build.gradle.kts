plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.coreCommonAndroid)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreResources)

  api(libs.accompanist.insetsUi)
  api(libs.androidx.compose.foundation)
  api(libs.androidx.compose.material)
  api(libs.androidx.compose.material3)
  api(libs.arrow.core)

  implementation(libs.androidx.compose.material3.windowSizeClass)
  implementation(libs.androidx.compose.materialIconsExtended)
  implementation(libs.androidx.compose.uiUtil)
  implementation(libs.androidx.lifecycle.runtime)
  implementation(libs.coil.coil)
  implementation(libs.coil.compose)
  implementation(libs.slimber)
}

android {
  namespace = "com.hedvig.android.core.ui"
}
