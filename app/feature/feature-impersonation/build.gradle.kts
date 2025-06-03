plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

hedvig {
  compose()
}

dependencies {
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.activity.core)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.uiCore)
  implementation(libs.androidx.lifecycle.runtime)
  implementation(libs.androidx.lifecycle.viewModel)
  implementation(libs.koin.android)
  implementation(libs.koin.core)
  implementation(projects.authCorePublic)
  implementation(projects.authlib)
  implementation(projects.designSystemHedvig)
  implementation(projects.navigationCore)
}
