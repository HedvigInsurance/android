@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
  alias(libs.plugins.serialization)
}

android {
  namespace = "com.hedvig.android.core.ui"

  buildFeatures {
    viewBinding = true
  }
}

dependencies {
  implementation(projects.app.core.commonAndroid)
  implementation(projects.app.core.designSystem)
  implementation(projects.app.core.resources)
  implementation(projects.app.apollo.octopus)

  api(libs.accompanist.insetsUi)
  api(libs.androidx.compose.foundation)
  api(libs.androidx.compose.material)
  api(libs.androidx.compose.material3)
  api(libs.arrow.core)

  implementation(libs.androidx.compose.material3.windowSizeClass)
  implementation(libs.androidx.compose.materialIconsExtended)
  implementation(libs.androidx.compose.uiUtil)
  implementation(libs.androidx.lifecycle.runtime)
  implementation(libs.androidx.other.core)
  implementation(libs.coil.coil)
  implementation(libs.coil.compose)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.slimber)
}
