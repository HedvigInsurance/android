plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.coreDesignSystem)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
  implementation(projects.hanalyticsCore)
  implementation(projects.navigationCore)
  implementation(projects.navigationComposeTyped)

  implementation(libs.androidx.other.activityCompose)
  implementation(libs.androidx.compose.material)
  implementation(libs.androidx.compose.material3.windowSizeClass)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.koin.android)
}

android {
  namespace = "com.hedvig.android.feature.businessmodel"
}
