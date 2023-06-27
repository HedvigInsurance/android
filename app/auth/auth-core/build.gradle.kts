@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.serialization)
}

dependencies {
  implementation(projects.app.auth.authEventCore)
  implementation(projects.app.core.common)
  implementation(projects.app.core.commonAndroid)
  implementation(projects.app.core.datastore)

  api(libs.hedvig.authlib)
  // do not remove ktor, authlib has an old ktor version which somehow crashes. Remove when we bump authlib.
  implementation(libs.ktor)

  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okhttp.core)
  implementation(libs.slimber)

  testImplementation(projects.app.auth.authTest)
  testImplementation(projects.app.core.commonTest)
  testImplementation(projects.app.core.datastoreTest)

  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.okhttp.mockWebServer)
  testImplementation(libs.turbine)
}

android {
  namespace = "com.hedvig.android.auth"
}
