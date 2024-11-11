plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
}

dependencies {
  api(libs.okhttp.core)

  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
}
