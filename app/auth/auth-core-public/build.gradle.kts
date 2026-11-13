plugins {
  id("hedvig.multiplatform.library.ios")
  id("hedvig.gradle.plugin")
}

hedvig {
  serialization()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.kotlinx.datetime)
      api(projects.authCoreApi)
      api(libs.ktor.client.core)
      api(projects.authlib)
      implementation(libs.androidx.datastore.core)
      implementation(libs.androidx.datastore.preferencesCore)
      implementation(libs.coroutines.core)
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.uuid)
      implementation(projects.authEventCore)
      implementation(projects.coreBuildConstants)
      implementation(projects.coreCommonPublic)
      implementation(projects.initializable)
    }
    jvmTest.dependencies {
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.junit)
      implementation(libs.ktor.client.mock)
      implementation(libs.turbine)
      implementation(projects.authCoreTest)
      implementation(projects.coreDatastoreTest)
      implementation(projects.loggingTest)
      implementation(projects.testClock)
    }
  }
}
