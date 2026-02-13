plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  serialization()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.coroutines.core)
      implementation(libs.koin.core)
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
