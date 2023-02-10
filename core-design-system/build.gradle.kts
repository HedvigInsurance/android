plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
}

dependencies {
  api(libs.accompanist.insetsUi)
  api(libs.androidx.compose.foundation)
  api(libs.androidx.compose.material)

  implementation(libs.accompanist.themeAdapter.material)
}

android {
  namespace = "com.hedvig.android.core.designsystem"

  defaultConfig {
    @Suppress("UnstableApiUsage")
    vectorDrawables.useSupportLibrary = true
  }
}
