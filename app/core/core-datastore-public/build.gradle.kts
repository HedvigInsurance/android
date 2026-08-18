plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
  id("com.rickclephas.kmp.nativecoroutines")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.androidx.datastore.core)
      api(libs.androidx.datastore.preferencesCore)

      implementation(libs.coroutines.core)
      implementation(libs.uuid)
      implementation(projects.coreCommonPublic)
    }
  }
}
