plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  implementation(libs.androidx.other.appCompat)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreResources)
  implementation(projects.languageCore)
}
