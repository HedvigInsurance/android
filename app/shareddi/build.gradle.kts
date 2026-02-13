import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.apollo.api)
      implementation(libs.apollo.engine.ktor)
      implementation(libs.apollo.normalizedCache)
      implementation(libs.apollo.runtime)
      implementation(libs.koin.core)
      implementation(libs.ktor.client.auth)
      implementation(libs.ktor.client.logging)
      implementation(projects.authCoreApi)
      implementation(projects.coreBuildConstants)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreDatastorePublic)
      implementation(projects.featureClaimChat)
      implementation(projects.featureFlags)
      implementation(projects.languageCore)
      implementation(projects.networkClients)
    }
  }
}
