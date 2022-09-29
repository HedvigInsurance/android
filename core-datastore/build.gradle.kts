plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.coreCommonAndroid)

  api(libs.androidx.datastore.preferencesCore)
  api(libs.androidx.datastore.preferences)
  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
}
