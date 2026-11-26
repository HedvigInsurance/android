plugins {
  id("hedvig.multiplatform.library.ios")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.coreCommonPublic)
    }
    jvmTest.dependencies {
      implementation(libs.junit)
    }
    androidMain.dependencies {
      implementation(libs.androidx.other.appCompat)
    }
  }
}
