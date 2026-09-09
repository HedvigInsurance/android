plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
  id("com.rickclephas.kmp.nativecoroutines")
}

hedvig {
  serialization()
}

kotlin {
  androidLibrary {
    withHostTest {}
  }
  sourceSets {
    commonMain.dependencies {
      implementation(libs.coroutines.core)
    }
    androidMain.dependencies {
      implementation(libs.unleash)
      implementation(projects.authCorePublic)
      implementation(projects.authEventCore)
      implementation(projects.coreBuildConstants)
      implementation(projects.coreCommonPublic)
    }
    getByName("androidHostTest").dependencies {
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.junit)
      implementation(libs.turbine)
      implementation(projects.loggingTest)
    }
  }
}
