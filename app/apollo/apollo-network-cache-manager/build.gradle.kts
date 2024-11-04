plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.apollo.normalizedCache)
  implementation(libs.apollo.runtime)
  implementation(libs.koin.core)
}
