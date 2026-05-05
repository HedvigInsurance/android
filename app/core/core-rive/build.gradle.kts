plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  implementation(projects.coreCommonPublic)
  implementation(projects.loggingPublic)
  implementation(libs.rive.android)
}