plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.coreResources)

  api(libs.accompanist.insetsUi)
  api(libs.androidx.compose.foundation)
  api(libs.androidx.compose.material)

  implementation(libs.accompanist.themeAdapter.material)
  implementation(libs.androidx.compose.material3)
}

android {
  namespace = "com.hedvig.android.core.designsystem"

  defaultConfig {
    @Suppress("UnstableApiUsage")
    vectorDrawables.useSupportLibrary = true
  }
}
