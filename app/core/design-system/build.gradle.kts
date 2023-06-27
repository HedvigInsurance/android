plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.app.core.resources)

  api(libs.androidx.compose.foundation)
  api(libs.androidx.compose.material3)

  implementation(libs.accompanist.systemUiController)
  implementation(libs.androidx.compose.material)
}

android {
  namespace = "com.hedvig.android.core.designsystem"

  defaultConfig {
    vectorDrawables.useSupportLibrary = true
  }
}
