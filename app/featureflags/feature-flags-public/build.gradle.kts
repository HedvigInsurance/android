plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
  api(libs.unleash)
  implementation(projects.authCorePublic)
  implementation(projects.authEventCore)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreCommonPublic)
  implementation(projects.marketCore)
}
