plugins {
  id("hedvig.kotlin.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  api(projects.authEventCore)

  implementation(libs.turbine)
}
