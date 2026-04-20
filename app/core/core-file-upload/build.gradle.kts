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
      implementation(libs.arrow.core)
      implementation(libs.koin.core)
      implementation(libs.kotlinx.serialization.core)
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.ktor.client.core)
      implementation(libs.uri.kmp)
      api(projects.coreBuildConstants)
      api(projects.coreCommonPublic)
      implementation(projects.networkClients)
    }

    androidMain.dependencies {
      api(libs.androidx.compose.foundation)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.coreResources)
      implementation(projects.designSystemHedvig)
    }
  }
}
