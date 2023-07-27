plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
}

dependencies {
  implementation(projects.coreCommonPublic)
  implementation(projects.theme)

  api(libs.androidx.datastore.core)
  api(libs.androidx.datastore.preferencesCore)

  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
}
