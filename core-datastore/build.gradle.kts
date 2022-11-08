plugins {
  id("hedvig.kotlin.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.coreCommon)

  implementation(libs.androidx.datastore.core)
  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
}
