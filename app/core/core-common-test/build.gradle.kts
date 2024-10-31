plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.arrow.core)
  implementation(libs.assertK)
  implementation(libs.coroutines.test)
  implementation(libs.junit)
}
