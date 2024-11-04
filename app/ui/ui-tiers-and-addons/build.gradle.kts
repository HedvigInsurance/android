plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(projects.composeUi)
  implementation(projects.coreResources)
  implementation(projects.dataContractAndroid)
  implementation(projects.dataProductVariantPublic)
  implementation(projects.designSystemHedvig)
}
