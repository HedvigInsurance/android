plugins {
  id("hedvig.android.library.kotlin")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(libs.coroutines.core)
  implementation(libs.okhttp.core)
}
