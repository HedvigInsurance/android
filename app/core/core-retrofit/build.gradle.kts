plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(libs.retrofitArrow)
  api(projects.coreCommonPublic)
}
