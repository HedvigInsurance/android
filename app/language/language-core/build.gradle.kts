plugins {
  id("hedvig.multiplatform.library.ios")
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
      implementation(projects.coreResources)
    }
    commonTest.dependencies {
      implementation(libs.assertK)
      implementation(libs.kotlin.test)
    }
    jvmTest.dependencies {
      implementation(libs.junit)
      implementation(projects.loggingTest)
    }
    androidMain.dependencies {
      implementation(libs.androidx.other.appCompat)
    }
  }
}

