plugins {
  id("hedvig.kotlin.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

dependencies {
  api(projects.moleculePublic)
  implementation(libs.androidx.annotation)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.molecule)
  implementation(libs.turbine)
}
