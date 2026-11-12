plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.jetbrains.compose.runtime)
      implementation(projects.coreResources)
      implementation(projects.designSystemHedvig)
    }
    androidMain.dependencies {
      implementation(libs.accompanist.permissions)
      implementation(libs.androidx.activity.compose)
      implementation(libs.jetbrains.compose.ui)
    }
  }
}
