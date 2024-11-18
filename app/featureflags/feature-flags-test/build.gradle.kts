plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
}

dependencies {
  implementation(libs.turbine)
  implementation(projects.featureFlagsPublic)
}
