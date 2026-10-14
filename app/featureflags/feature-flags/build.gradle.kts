plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
  id("com.rickclephas.kmp.nativecoroutines")
}

hedvig {
  serialization()
}

kotlin {
  android {
    withHostTest {}
  }
  sourceSets {
    commonMain.dependencies {
      implementation(libs.coroutines.core)
    }
    androidMain.dependencies {
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.unleash)
      implementation(projects.authCorePublic)
      implementation(projects.authEventCore)
      implementation(projects.coreBuildConstants)
      implementation(projects.coreCommonPublic)
    }
    getByName("androidHostTest").dependencies {
      implementation(libs.assertK)
      implementation(libs.junit)
      implementation(libs.kotlin.test)
      implementation(projects.loggingTest)
    }
  }
}

// CI runs the root `test` task, which only reaches modules that have a task with that exact name.
tasks.register("test") {
  dependsOn("testAndroidHostTest")
}
