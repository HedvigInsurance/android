plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.atomicfu)
    }
  }
}
