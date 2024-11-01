plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
  id("hedvig.kotlin.library.compose")
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.androidx.compose.runtime)
  implementation(libs.coroutines.core)
}
