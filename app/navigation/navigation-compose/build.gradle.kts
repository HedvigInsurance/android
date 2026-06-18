plugins {
  id("hedvig.multiplatform.library")
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
      implementation(libs.jetbrains.compose.runtime)
      implementation(libs.kotlinx.serialization.core)
      implementation(libs.kotlinx.serialization.json)
      implementation(projects.navigationCommon)
    }
    commonTest.dependencies {
      implementation(libs.assertK)
      implementation(libs.kotlin.test)
    }
    androidMain.dependencies {
      implementation(libs.androidx.navigation3.ui)
      implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    }
    jvmMain.dependencies {
      implementation(libs.ktor.client.core)
    }
    nativeMain.dependencies {
      implementation(libs.ktor.client.core)
    }
  }
}
