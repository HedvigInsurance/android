plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
}

dependencies {
  implementation(libs.arrow.core)
  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
  implementation(projects.coreCommonPublic)
}
