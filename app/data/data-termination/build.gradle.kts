plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.squareSortDependencies)
  alias(libs.plugins.apollo)
}

dependencies {
  apolloMetadata(projects.apolloOctopusPublic)
  implementation(libs.apollo.runtime)
  implementation(libs.apollo.normalizedCache)
  implementation(libs.arrow.core)
  implementation(libs.koin.core)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreUiData)
  implementation(projects.coreCommonPublic)
  implementation(projects.dataContractPublic)
  implementation(libs.kotlinx.datetime)
  implementation(projects.featureFlagsPublic)
}

apollo {
  service("octopus") {
    packageName.set("octopus")
  }
}
