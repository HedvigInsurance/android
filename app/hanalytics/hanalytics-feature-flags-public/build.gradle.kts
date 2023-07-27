plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.koin.core)
  implementation(projects.coreCommonPublic)
  implementation(projects.hanalyticsCore)
  implementation(projects.marketCore)
}
