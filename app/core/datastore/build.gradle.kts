plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
}

dependencies {
  implementation(projects.app.core.common)

  implementation(libs.androidx.datastore.core)
  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
}
