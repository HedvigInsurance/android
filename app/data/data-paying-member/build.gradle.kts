plugins {
  id("hedvig.kotlin.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
}

dependencies {
  implementation(libs.arrow.core)
  implementation(libs.koin.core)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDemoMode)
  implementation(projects.dataContractPublic)
}
