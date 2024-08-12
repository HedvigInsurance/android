plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreResources)
  implementation(projects.dataContractPublic)
  implementation(projects.dataProductVariantPublic)
}
