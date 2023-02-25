plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
}

android {
  namespace = "com.hedvig.android.cancelinsurance"
}

dependencies {
  implementation(projects.apollo)
  implementation(projects.auth.authAndroid)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreResources)
  implementation(projects.coreUi)

  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material3.windowSizeClass)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.other.activityCompose)
  implementation(libs.arrow.core)
  implementation(libs.coroutines.core)
  implementation(libs.koin.android)
  implementation(libs.kotlinx.datetime)
  implementation(libs.slimber)
  implementation(libs.timber)
}
