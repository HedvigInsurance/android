plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

dependencies {
  implementation(libs.androidx.annotation)
  implementation(projects.coreResources)
  implementation(projects.marketCore)
}
