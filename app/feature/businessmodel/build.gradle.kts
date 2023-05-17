plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.app.auth.authAndroid)
  implementation(projects.app.core.commonAndroid)
  implementation(projects.app.core.designSystem)
  implementation(projects.app.core.resources)
  implementation(projects.app.core.ui)
  implementation(projects.app.hanalytics.hanalyticsCore)

  implementation(libs.androidx.other.appCompat)
  implementation(libs.androidx.other.activityCompose)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.material3.windowSizeClass)
  implementation(libs.koin.android)
}

android {
  namespace = "com.hedvig.android.feature.businessmodel"
}
