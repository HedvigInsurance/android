plugins {
  id("hedvig.multiplatform.library.ios")
  id("hedvig.gradle.plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.apollo.normalizedCache)
      implementation(libs.apollo.runtime)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.authEventCore)
      implementation(projects.coreCommonPublic)
    }
  }
}
