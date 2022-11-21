plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(libs.koin.android)
  implementation(libs.okhttp.core)
}
