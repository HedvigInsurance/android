plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.koin.core)
    }
    androidMain.dependencies {
      implementation(libs.androidx.other.appCompat)
    }
  }
}
