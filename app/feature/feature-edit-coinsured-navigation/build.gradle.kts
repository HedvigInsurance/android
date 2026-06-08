plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  serialization()
  navKeys()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.coreCommonPublic)
      implementation(projects.dataCoinsured)
      implementation(projects.navigationCommon)
    }
  }
}
