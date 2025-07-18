plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.apollo.api)
      api(libs.apollo.runtime)
      api(libs.arrow.core)
      api(libs.coroutines.core)
      api(projects.coreCommonPublic)
      api(projects.apolloOperationError)

      implementation(libs.apollo.normalizedCache)
    }
  }
}
