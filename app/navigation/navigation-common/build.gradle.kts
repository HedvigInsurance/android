plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  serialization()
}

kotlin {
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
