plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.uri.kmp)
      implementation(libs.jetbrains.compose.runtime)
    }
    androidMain.dependencies {
      implementation(libs.androidx.activity.compose)
      implementation(libs.androidx.activity.core)
    }
  }
}
