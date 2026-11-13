plugins {
  id("hedvig.multiplatform.library.ios")
  id("hedvig.gradle.plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.coroutines.core)
      implementation(projects.authEventCore)
      implementation(projects.coreCommonPublic)
    }
    jvmMain.dependencies {
      implementation(libs.androidx.datastore.core)
      implementation(libs.androidx.datastore.preferencesCore)
    }
  }
}
