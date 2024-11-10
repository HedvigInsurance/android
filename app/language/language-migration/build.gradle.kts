hedvig {}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

dependencies {
  implementation(libs.androidx.other.appCompat)
  implementation(libs.koin.core)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreResources)
  implementation(projects.languageCore)
  implementation(projects.marketCore)
  implementation(projects.marketSet)
}
