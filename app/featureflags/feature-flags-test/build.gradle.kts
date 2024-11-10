hedvig {}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

dependencies {
  implementation(libs.turbine)
  implementation(projects.featureFlagsPublic)
}
