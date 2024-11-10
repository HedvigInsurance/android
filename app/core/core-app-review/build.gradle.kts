plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
}
dependencies {
  implementation(libs.androidx.datastore.core)
  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
  implementation(projects.authEventCore)
}
