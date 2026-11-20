plugins {
  id("hedvig.multiplatform.library.ios")
  id("hedvig.gradle.plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.apollo.api)
      implementation(libs.arrow.core)
      implementation(projects.apolloOperationError)
      implementation(projects.coreCommonPublic)
    }
    commonTest.dependencies {
      implementation(libs.assertK)
      implementation(projects.apolloCore)
      implementation(libs.coroutines.test)
      implementation(libs.kotlin.test)
    }
  }
}
