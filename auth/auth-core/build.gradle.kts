@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.serialization)
}

dependencies {
  implementation(projects.coreCommon)
  implementation(projects.coreDatastore)

  api(libs.authlib)

  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.arrowKt.core)
  implementation(libs.authlib)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.datetime)
  implementation(libs.okhttp.core)
  implementation(libs.serialization.json)
  implementation(libs.slimber)

  testImplementation(projects.auth.authTest)
  testImplementation(projects.coreCommonTest)
  testImplementation(projects.coreDatastoreTest)

  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.okhttp.mockWebServer)
  testImplementation(libs.turbine)
}
