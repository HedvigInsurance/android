plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  serialization()
  compose()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.jetbrains.navigation.compose)
      implementation(libs.jetbrains.compose.runtime)
      implementation(libs.kotlinx.serialization.core)
      implementation(libs.kotlinx.serialization.json)
      implementation(projects.navigationCommon)
    }
    jvmMain.dependencies {
      implementation(libs.ktor.client.core)
    }
    nativeMain.dependencies {
      implementation(libs.ktor.client.core)
    }
  }
}
