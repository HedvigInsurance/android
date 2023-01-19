plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
}

android {
  defaultConfig {
    @Suppress("UnstableApiUsage")
    vectorDrawables.useSupportLibrary = true
  }
}

dependencies {
  api(libs.accompanist.insetsUi)
  api(libs.androidx.compose.foundation)
  api(libs.androidx.compose.material)
  api(libs.androidx.compose.uiToolingPreview)
  implementation(libs.androidx.compose.mdcAdapter)

  debugApi(libs.androidx.compose.uiTooling)
}
