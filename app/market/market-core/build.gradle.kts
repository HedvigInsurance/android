plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
}

dependencies {
  api(projects.languageCore)

  implementation(libs.androidx.datastore.core)
  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.koin.core)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreCommonPublic)
}
