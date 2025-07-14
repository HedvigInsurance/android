plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  serialization()
  compose()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.androidx.compose.runtime)
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.apolloOctopusPublic)
    }
  }
}
