plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(projects.languageCore)
}
