plugins {
  id("hedvig.multiplatform.library.ios")
  id("hedvig.gradle.plugin")
  id("com.rickclephas.kmp.nativecoroutines")
}

hedvig {
  apollo("octopus") {
    generateAsInternal = true
  }
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.apollo.normalizedCache)
      implementation(libs.apollo.runtime)
      implementation(libs.arrow.core)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.coreCommonPublic)
    }
    commonTest.dependencies {
      implementation(libs.apollo.engine.ktor)
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.kotlin.test)
      implementation(libs.ktor.client.mock)
    }
    nativeMain.dependencies {
      implementation(projects.iosDataBridge)
    }
  }
}
