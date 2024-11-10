plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
}

dependencies {
  api(projects.authEventCore)

  implementation(libs.turbine)
}
