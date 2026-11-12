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
      implementation(projects.coreCommonPublic)
      implementation(projects.featureFlags)
      implementation(projects.theme)
    }
    jvmTest.dependencies {
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.junit)
    }
  }
}
