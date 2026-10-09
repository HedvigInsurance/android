plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.coroutines.core)
      implementation(libs.androidx.annotation)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreLocale)
    }
    commonTest.dependencies {
      implementation(libs.assertK)
      implementation(libs.kotlin.test)
    }
    androidMain.dependencies {
      implementation(libs.androidx.other.appCompat)
      implementation(projects.coreResources)
    }
  }
}

