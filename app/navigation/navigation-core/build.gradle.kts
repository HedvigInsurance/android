plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.serialization)
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(libs.androidx.navigation.common)

  implementation(libs.androidx.annotation)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.coreBuildConstants)
  implementation(projects.dataContractPublic)
  implementation(projects.navigationCompose)
}
