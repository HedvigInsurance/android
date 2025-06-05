plugins {
  id("hedvig.kotlin.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  api(libs.androidx.datastore.core)
  api(libs.androidx.datastore.preferencesCore)

  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
  implementation(projects.coreCommonPublic)
}
