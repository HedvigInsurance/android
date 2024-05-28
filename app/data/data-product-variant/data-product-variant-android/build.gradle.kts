plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.kotlinx.immutable.collections)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreResources)
  implementation(projects.dataContractAndroid)
  implementation(projects.dataContractPublic)
  implementation(projects.dataProductVariantPublic)
}
