plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.atomicfu)
      implementation(projects.apolloOperationError)
    }
    androidMain.dependencies {
      implementation(libs.slimber)
      implementation(libs.timber)
    }
  }
}
