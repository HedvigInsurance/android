plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
  id("hedvig.kotlin.library.compose")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(projects.moleculePublic)
  implementation(libs.turbine)
  implementation(libs.molecule)
}
