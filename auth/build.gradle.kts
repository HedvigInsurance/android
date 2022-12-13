@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.serialization)
}

dependencies {
  implementation(projects.coreCommon)
  implementation(projects.coreDatastore)

  api(libs.authlib)

  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.arrowKt.core)
  implementation(libs.koin.android)
  implementation(libs.okhttp.core)
  implementation(libs.serialization.json)
  implementation(libs.authlib)

  testImplementation(projects.authTest)
  testImplementation(projects.coreDatastoreTest)

  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.okhttp.mockWebServer)
  testImplementation(libs.turbine)
}
