plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.serialization)
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.androidx.compose.runtime)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreResources)
}
