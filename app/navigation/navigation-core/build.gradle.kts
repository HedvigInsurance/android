plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.serialization)
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.androidx.annotation)
  api(libs.androidx.navigation.common)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.coreBuildConstants)
  implementation(projects.dataContractPublic)
  implementation(projects.navigationCompose)
}
