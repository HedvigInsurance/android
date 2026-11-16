plugins {
  id("hedvig.multiplatform.library.ios")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.jetbrains.lifecycle.viewmodel)
      api(libs.molecule)
      implementation(libs.coroutines.core)
      implementation(libs.jetbrains.compose.runtime)
      implementation(libs.coroutines.core)
    }
    nativeMain.dependencies {
      implementation("com.rickclephas.kmp:kmp-nativecoroutines-core:${libs.versions.kmpNativeCoroutines.get()}")
    }
    nativeTest.dependencies {
      implementation(libs.assertK)
    }
    jvmTest.dependencies {
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.junit)
      implementation(libs.turbine)
    }
  }
}
