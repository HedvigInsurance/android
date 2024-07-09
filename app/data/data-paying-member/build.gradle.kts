plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.apollo)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {

  implementation(libs.arrow.core)
  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDemoMode)
  implementation(projects.dataContractPublic)
}

apollo {
  service("octopus") {
    packageName.set("octopus")
    dependsOn(projects.apolloOctopusPublic)
  }
}
