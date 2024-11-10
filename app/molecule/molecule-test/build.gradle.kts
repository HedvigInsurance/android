hedvig {}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
  id("hedvig.kotlin.library.compose")
}

dependencies {
  api(projects.moleculePublic)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.molecule)
  implementation(libs.turbine)
}
