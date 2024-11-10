hedvig {
}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

dependencies {
  api(libs.apollo.api)
  api(libs.apollo.runtime)
  api(libs.arrow.core)
  api(libs.coroutines.core)

  implementation(libs.apollo.normalizedCache)
  implementation(projects.coreCommonPublic)
}
