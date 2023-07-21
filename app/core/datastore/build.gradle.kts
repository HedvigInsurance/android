plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
}

dependencies {
  implementation(projects.app.core.common)
  implementation(projects.app.theme)

  api(libs.androidx.datastore.core)
  api(libs.androidx.datastore.preferencesCore)

  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
}
