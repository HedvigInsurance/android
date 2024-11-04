plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.activity.core)
}
