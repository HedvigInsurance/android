plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.jetbrains.navigation.common)
      implementation(libs.koin.core)
      implementation(projects.coreBuildConstants)
      implementation(projects.navigationCommon)
    }
  }
}
