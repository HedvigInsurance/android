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
  }
}
