plugins {
  id("hedvig.multiplatform.library.ios")
  id("hedvig.gradle.plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.coreCommonPublic)
    }
    jvmMain.dependencies {
      implementation(libs.apollo.normalizedCache)
      implementation(libs.apollo.runtime)
    }
  }
}
