import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

kotlin {
  @OptIn(ExperimentalKotlinGradlePluginApi::class)
  applyDefaultHierarchyTemplate {
    common {
      group("mobile") {
        withAndroidLibraryTarget()
        withNative()
      }
    }
  }
  sourceSets {
    commonMain.dependencies {
      implementation(libs.apollo.api)
      implementation(libs.apollo.engine.ktor)
      implementation(libs.apollo.normalizedCache)
      implementation(libs.apollo.runtime)
      implementation(libs.koin.core)
      implementation(libs.ktor.client.auth)
      implementation(projects.authCoreApi)
      implementation(projects.coreBuildConstants)
      implementation(projects.coreCommonPublic)
      implementation(projects.featureClaimChat)
    }
    val mobileMain by getting {
      dependencies {
        implementation(libs.datadog.sdk.ktor)
      }
    }
  }
}
