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
  api(libs.androidx.compose.material3)

  implementation(libs.accompanist.systemUiController)
}

android {
  namespace = "com.hedvig.android.core.designsystem"

  defaultConfig {
    @Suppress("UnstableApiUsage")
    vectorDrawables.useSupportLibrary = true
  }
}
