plugins {
  id("hedvig.kotlin.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  implementation(libs.arrow.core)
  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
  implementation(projects.coreCommonPublic)
}
