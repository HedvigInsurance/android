plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(libs.assertK)
  api(libs.junit)

  implementation(libs.arrow.core)
  implementation(libs.coroutines.test)
}
