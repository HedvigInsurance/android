plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  api(libs.androidx.datastore.preferences)
  implementation(libs.coroutines.core)
  implementation(libs.koin.android)
}
