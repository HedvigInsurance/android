plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.coroutines.core)
  implementation(libs.hAnalytics)
  implementation(libs.koin.core)
  implementation(libs.unleash)
  implementation(projects.authEventCore)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreCommonPublic)
  implementation(projects.hanalyticsCore)
  implementation(projects.marketCore)
}
