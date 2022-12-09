plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.auth)

  implementation(libs.authlib)
  implementation(libs.coroutines.core)
}
