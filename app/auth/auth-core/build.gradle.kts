plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.serialization)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(libs.hedvig.authlib)

  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.json)
  // do not remove ktor, authlib has an old ktor version which somehow crashes. Remove when we bump authlib.
  implementation(libs.ktor)
  implementation(libs.okhttp.core)
  implementation(projects.authEventCore)
  implementation(projects.coreCommonAndroidPublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDatastorePublic)
  implementation(projects.loggingPublic)
  implementation(projects.testClock)

  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.okhttp.mockWebServer)
  testImplementation(libs.turbine)
  testImplementation(projects.authTest)
  testImplementation(projects.coreCommonTest)
  testImplementation(projects.coreDatastoreTest)
  testImplementation(projects.loggingTest)
}
