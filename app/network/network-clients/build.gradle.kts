import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  id("hedvig.multiplatform.library.ios")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
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
      implementation(libs.arrow.core)
      implementation(libs.ktor.client.auth)
      implementation(libs.ktor.client.logging)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.authCoreApi)
      implementation(projects.authCorePublic)
      implementation(projects.coreBuildConstants)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreDatastorePublic)
      implementation(projects.coreDemoMode)
      implementation(projects.languageCore)
      implementation(projects.trackingCore)
    }
    getByName("mobileMain") {
      dependencies {
        implementation(libs.datadog.sdk.ktor)
        implementation(projects.datadogCore)
      }
    }
    androidMain.dependencies {
      implementation(libs.ktor.client.okhttp)
    }
    jvmMain.dependencies {
      implementation(libs.ktor.client.okhttp)
    }
    jvmTest.dependencies {
      implementation(libs.apollo.testingSupport)
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.junit)
      implementation(projects.authCoreTest)
      implementation(projects.loggingTest)
    }
    nativeMain.dependencies {
      implementation(libs.ktor.client.darwin)
    }
  }
}
