plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.androidx.annotation)
      implementation(libs.koin.core)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreLocale)
    }
    androidMain.dependencies {
      implementation(libs.androidx.other.appCompat)
      implementation(projects.coreResources)
      implementation(projects.languageCore)
    }
  }
}

