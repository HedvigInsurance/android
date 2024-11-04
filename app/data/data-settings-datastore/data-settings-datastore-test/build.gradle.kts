plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.coroutines.core)
  implementation(libs.turbine)
  implementation(projects.dataSettingsDatastorePublic)
  implementation(projects.theme)
}
