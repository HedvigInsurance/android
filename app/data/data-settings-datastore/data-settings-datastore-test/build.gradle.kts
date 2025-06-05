plugins {
  id("hedvig.kotlin.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  implementation(libs.coroutines.core)
  implementation(libs.turbine)
  implementation(projects.dataSettingsDatastorePublic)
  implementation(projects.theme)
}
