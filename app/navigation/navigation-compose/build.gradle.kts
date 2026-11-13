plugins {
  id("hedvig.multiplatform.library.ios")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  serialization()
  compose()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.androidx.navigation3.runtime)
      implementation(libs.jetbrains.compose.animation)
      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.compose.runtime)
      implementation(libs.jetbrains.compose.ui)
      implementation(libs.jetbrains.lifecycle.viewmodel.navigation3)
      implementation(libs.jetbrains.navigation3.ui)
      implementation(libs.kotlinx.serialization.core)
      implementation(libs.kotlinx.serialization.json)
      implementation(projects.designSystemHedvig)
      api(projects.navigationCommon)
    }
    commonTest.dependencies {
      implementation(libs.assertK)
      implementation(libs.kotlin.test)
    }
    jvmMain.dependencies {
      implementation(libs.ktor.client.core)
    }
    nativeMain.dependencies {
      implementation(libs.ktor.client.core)
    }
  }
}
