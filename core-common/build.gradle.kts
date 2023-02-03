plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
}

dependencies {
  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
  implementation(libs.okhttp.core)
}
