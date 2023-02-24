plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
}

dependencies {
  api(projects.auth.authEvent)

  implementation(libs.turbine)
}
