plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.coreCommonPublic)
      implementation(projects.languageCore)
    }
    androidMain.dependencies {
      implementation(projects.coreResources)
    }
  }
}
