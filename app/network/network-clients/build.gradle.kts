plugins {
  id("hedvig.multiplatform.library.ios")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
}

kotlin {
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
    androidMain.dependencies {
      implementation(libs.datadog.sdk.ktor)
      implementation(libs.ktor.client.okhttp)
      implementation(projects.datadogCore)
    }
    jvmMain.dependencies {
      implementation(libs.ktor.client.okhttp)
    }
    jvmTest.dependencies {
      implementation(kotlin("test"))
      implementation(libs.apollo.testingSupport)
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.junit)
      implementation(libs.ktor.client.mock)
      implementation(projects.authCoreTest)
      implementation(projects.loggingTest)
    }
    nativeMain.dependencies {
      implementation(libs.ktor.client.darwin)
    }
  }
}
