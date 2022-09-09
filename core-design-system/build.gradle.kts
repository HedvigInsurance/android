plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
}

android {
  defaultConfig {
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
  // TODO : Remove this dependency once we upgrade to Android Studio Dolphin b/228889042
  // More context: https://stackoverflow.com/a/71830120/9440211, https://github.com/android/nowinandroid/blob/88054edf909de54e582850fcc461b5fefb550289/build-logic/convention/src/main/kotlin/AndroidFeatureConventionPlugin.kt#L65-L71
  // This dependency is currently necessary to render Compose previews
  debugApi(libs.androidx.customview.poolingcontainer)
}
