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
      api(projects.dataSettingsDatastorePublic)
      implementation(libs.coroutines.core)
      implementation(libs.jetbrains.compose.foundation)
      implementation(projects.coreResources)
      implementation(projects.designSystemHedvig)
    }
    jvmTest.dependencies {
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.junit)
      implementation(libs.turbine)
      implementation(projects.loggingTest)
      implementation(projects.moleculeTest)
      implementation(projects.theme)
    }
  }
}
