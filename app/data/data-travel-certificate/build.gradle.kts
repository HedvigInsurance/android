plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.apollo.runtime)
  implementation(libs.arrow.core)
  implementation(libs.koin.core)
  implementation(libs.slimber)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.hanalyticsFeatureFlagsPublic)
}
