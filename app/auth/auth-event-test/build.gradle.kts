plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
}

dependencies {
  api(projects.app.auth.authEventCore)

  implementation(libs.turbine)
}
