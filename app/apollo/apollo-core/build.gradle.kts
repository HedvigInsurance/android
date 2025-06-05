plugins {
  id("hedvig.kotlin.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  api(libs.apollo.api)
  api(libs.apollo.runtime)
  api(libs.arrow.core)
  api(libs.coroutines.core)
  api(projects.coreCommonPublic)

  implementation(libs.apollo.normalizedCache)
}
