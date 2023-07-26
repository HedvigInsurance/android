plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
}

dependencies {
  api(projects.authEventCore)

  implementation(libs.turbine)
}
