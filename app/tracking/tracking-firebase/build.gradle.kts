plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  implementation(platform(libs.firebase.bom))
  implementation(libs.coroutines.core)
  implementation(libs.firebase.analytics)
  implementation(projects.authCorePublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDemoMode)
  implementation(projects.initializable)
  implementation(projects.trackingCore)
}
