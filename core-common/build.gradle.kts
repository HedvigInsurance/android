plugins {
  id("hedvig.kotlin.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
  implementation(libs.okhttp.core)
}
