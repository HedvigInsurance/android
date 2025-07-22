plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
}

// add apollo api dependency
kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.apollo.api)
    }
  }
}
