plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
}

dependencies {
  api(projects.auth.authEventCore)

  implementation(libs.turbine)
}
