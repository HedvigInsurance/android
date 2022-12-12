plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
}

dependencies {
  implementation(libs.authlib)
  implementation(libs.coroutines.core)
  implementation(libs.turbine)
}
