plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
  alias(libs.plugins.molecule)
}

dependencies {
  api(projects.app.molecule.moleculePublic)

  implementation(libs.turbine)
}
