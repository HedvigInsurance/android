plugins {
  id("hedvig.multiplatform.library.ios")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
  serialization()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.jetbrains.compose.runtime)
      implementation(libs.kotlinx.serialization.core)
    }
  }
}
