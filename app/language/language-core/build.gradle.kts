plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.androidx.annotation)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreLocale)
    }
    androidMain.dependencies {
      implementation(libs.androidx.other.appCompat)
      implementation(libs.koin.core)
      implementation(projects.coreResources)
      implementation(projects.languageCore)
    }
  }
}

