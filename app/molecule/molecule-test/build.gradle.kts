plugins {
  id("hedvig.jvm.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

dependencies {
  api(projects.moleculePublic)
  implementation(libs.androidx.annotation)
  implementation(libs.jetbrains.compose.runtime)
  implementation(libs.molecule)
  implementation(libs.turbine)
}
