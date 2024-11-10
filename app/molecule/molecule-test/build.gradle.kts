hedvig {
  compose()
}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
}

dependencies {
  api(projects.moleculePublic)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.molecule)
  implementation(libs.turbine)
}
