plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.androidx.other.appCompat)
  implementation(libs.koin.core)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreResources)
  implementation(projects.languageCore)
  implementation(projects.marketCore)
  implementation(projects.marketSet)
}
