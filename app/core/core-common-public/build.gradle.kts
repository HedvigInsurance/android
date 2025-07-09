plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.coroutines.core)
      implementation(libs.koin.core)
      implementation(libs.kotlinx.datetime)
    }
    jvmMain.dependencies {
      api(libs.okhttp.core)
    }
  }
}
