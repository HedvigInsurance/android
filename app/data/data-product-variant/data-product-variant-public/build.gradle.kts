plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  serialization()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.dataContract)
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.apolloOctopusPublic)
    }
  }
}
