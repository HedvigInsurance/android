plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.app.auth.authAndroid)
  implementation(projects.app.core.coreCommonAndroid)
  implementation(projects.app.core.coreDesignSystem)
  implementation(projects.app.core.coreResources)
  implementation(projects.app.core.coreUi)
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
