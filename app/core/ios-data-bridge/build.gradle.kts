plugins {
  id("hedvig.multiplatform.library.ios")
  id("hedvig.gradle.plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.apollo.api)
      implementation(libs.apollo.runtime)
      implementation(libs.atomicfu)
      implementation(libs.coroutines.core)
      implementation(projects.coreCommonPublic)
      implementation(projects.loggingPublic)
    }
    commonTest.dependencies {
      implementation(libs.assertK)
      implementation(libs.kotlin.test)
    }
    nativeMain.dependencies {
      implementation(libs.kmpNativeCoroutines.core)
    }
  }
}
