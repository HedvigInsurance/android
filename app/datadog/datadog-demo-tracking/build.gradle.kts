plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

dependencies {
  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDemoMode)
  implementation(projects.datadogCore)
  implementation(projects.initializable)
}
