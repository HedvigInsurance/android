plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
  viewModels()
}

dependencies {
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.activity.core)
  implementation(libs.jetbrains.compose.runtime)
  implementation(libs.jetbrains.compose.ui)
  implementation(libs.jetbrains.lifecycle.runtime)
  implementation(libs.jetbrains.lifecycle.viewmodel)
  implementation(libs.metro.viewmodel)
  implementation(libs.metro.viewmodel.compose)
  implementation(projects.authCorePublic)
  implementation(projects.authlib)
  implementation(projects.coreCommonPublic)
  implementation(projects.designSystemHedvig)
  implementation(projects.navigationCore)
}
