plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
  serialization()
  compose()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.apollo.api)
      implementation(libs.apollo.runtime)
      implementation(libs.arrow.core)
      implementation(libs.koin.core)
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.coreCommonPublic)
      implementation(projects.navigationCompose)
    }
    androidMain.dependencies {
      implementation(libs.androidx.navigation.compose)
      implementation(projects.designSystemHedvig)
      implementation(projects.moleculeAndroid)
      implementation(projects.moleculePublic)
      implementation(projects.navigationCommon)
    }
  }
}
