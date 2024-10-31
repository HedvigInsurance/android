plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.coroutines.core)
//  implementation(libs.hedvig.authlib)
  implementation(libs.turbine)
  implementation(projects.authCorePublic)
}
