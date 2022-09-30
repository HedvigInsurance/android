plugins {
  id("hedvig.android.library.kotlin")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
  implementation(libs.okhttp.core)
}
