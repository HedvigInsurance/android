plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.coroutines.core)
      implementation(libs.koin.core)
      implementation(libs.kotlinx.datetime)
    }

    androidMain.apply {
      dependencies {
        implementation(libs.androidx.other.core)
        implementation(projects.apolloOctopusPublic)
        implementation(projects.coreResources)
      }
    }
  }
}
