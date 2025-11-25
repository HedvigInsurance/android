plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.apollo.api)
      implementation(libs.apollo.normalizedCache)
      implementation(libs.apollo.runtime)
      implementation(libs.koin.core)
      implementation(libs.ktor.client.auth)
      implementation(projects.authCoreApi)
      implementation(projects.coreBuildConstants)
      implementation(projects.featureClaimChat)
    }
  }
}
