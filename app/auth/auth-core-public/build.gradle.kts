plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
}

hedvig {
  serialization()
}

dependencies {
  api(libs.kotlinx.datetime)
  api(libs.okhttp.core)
  api(projects.authlib)

  implementation(libs.androidx.datastore.core)
  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.ktor.client.android)
  implementation(projects.authEventCore)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreCommonPublic)
  implementation(projects.initializable)
  implementation(projects.testClock)

  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.okhttp.mockWebServer)
  testImplementation(libs.turbine)
  testImplementation(projects.authCoreTest)
  testImplementation(projects.coreDatastoreTest)
  testImplementation(projects.loggingTest)
}
