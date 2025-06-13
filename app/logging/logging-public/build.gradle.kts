plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
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
