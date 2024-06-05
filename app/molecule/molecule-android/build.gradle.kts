plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.squareSortDependencies)
  alias(libs.plugins.composeCompilerGradlePlugin)
}

dependencies {
  api(projects.moleculePublic)
  implementation(libs.androidx.lifecycle.viewModel)
  implementation(libs.coroutines.core)
  implementation(libs.molecule)
}
