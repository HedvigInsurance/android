plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(projects.languageCore)

  implementation(libs.androidx.datastore.core)
  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.koin.core)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreResources)
}
