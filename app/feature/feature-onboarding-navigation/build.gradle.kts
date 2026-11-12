plugins {
  id("hedvig.multiplatform.library.ios")
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
      implementation(libs.androidx.annotation)
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.coreCommonPublic)
      implementation(projects.navigationCommon)
    }
  }
}
