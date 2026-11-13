plugins {
  id("hedvig.multiplatform.library.ios")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  serialization()
}

kotlin {
  android {
    androidResources.enable = false
  }
  sourceSets {
    commonMain.dependencies {
      api(libs.androidx.navigation3.runtime)
      implementation(libs.kotlin.reflect)
    }
    commonTest.dependencies {
      implementation(libs.kotlinx.serialization.core)
    }
  }
}
