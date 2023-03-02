@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.serialization)
}

dependencies {
  implementation(projects.auth.authEventCore)
  implementation(projects.coreCommon)
  implementation(projects.coreCommonAndroid)
  implementation(projects.coreDatastore)

  api(libs.hedvig.authlib)

  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.arrow.core)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okhttp.core)
  implementation(libs.slimber)

  testImplementation(projects.auth.authTest)
  testImplementation(projects.coreCommonTest)
  testImplementation(projects.coreDatastoreTest)

  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.okhttp.mockWebServer)
  testImplementation(libs.turbine)
}

android {
  namespace = "com.hedvig.android.auth"
}
