plugins {
  id("hedvig.multiplatform.library.ios")
  id("hedvig.gradle.plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.androidx.datastore.core)
      implementation(libs.androidx.datastore.preferencesCore)
      implementation(libs.coroutines.core)
      implementation(projects.authEventCore)
      implementation(projects.coreCommonPublic)
    }
  }
}
