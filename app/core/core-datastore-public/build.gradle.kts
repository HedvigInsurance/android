plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.androidx.datastore.core)
      api(libs.androidx.datastore.preferencesCore)

      implementation(libs.coroutines.core)
      implementation(libs.koin.core)
      implementation(projects.coreCommonPublic)
    }
  }
}
