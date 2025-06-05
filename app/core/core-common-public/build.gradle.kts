plugins {
  id("hedvig.kotlin.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  api(libs.okhttp.core)

  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.datetime)
}
