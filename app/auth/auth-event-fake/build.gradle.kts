plugins {
  id("hedvig.jvm.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  api(projects.authEventCore)

  implementation(libs.turbine)
}
