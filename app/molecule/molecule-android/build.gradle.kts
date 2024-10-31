plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(projects.moleculePublic)
  implementation(libs.androidx.lifecycle.viewModel)
  implementation(libs.coroutines.core)
  implementation(libs.molecule)
}
