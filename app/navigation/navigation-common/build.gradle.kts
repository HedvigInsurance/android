plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.androidx.navigation3.runtime)
      implementation(libs.kotlin.reflect)
    }
  }
}
