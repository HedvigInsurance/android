plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.coreResources)
  implementation(projects.coreDesignSystem)

  api(libs.accompanist.insets)
  api(libs.accompanist.insetsUi)
  api(libs.androidx.compose.foundation)
  api(libs.androidx.compose.material)
  api(libs.androidx.compose.uiToolingPreview)
  debugApi(libs.androidx.compose.uiTooling)
  implementation(libs.androidx.compose.mdcAdapter)
  implementation(libs.coil.coil)
}
