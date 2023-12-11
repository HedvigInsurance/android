plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(libs.arrow.core)
  api(libs.retrofit)
  api(libs.retrofitArrow)
  api(projects.coreCommonPublic)
}
