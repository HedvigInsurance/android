plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.coreResources)
  implementation(projects.coreDesignSystem)

  api(libs.accompanist.insetsUi)
  api(libs.androidx.compose.foundation)
  api(libs.androidx.compose.material)

  implementation(libs.androidx.compose.uiUtil)
  implementation(libs.coil.coil)
}

android {
  namespace = "com.hedvig.android.core.ui"
}
