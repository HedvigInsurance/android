plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
}

dependencies {
  implementation(libs.coroutines.core)
  implementation(libs.hedvig.authlib)
  implementation(libs.turbine)
  implementation(projects.authCorePublic)
}
