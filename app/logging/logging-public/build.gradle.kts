plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.atomicfu)
    }
    androidMain.dependencies {
      implementation(libs.slimber)
      implementation(libs.timber)
    }
  }
}
