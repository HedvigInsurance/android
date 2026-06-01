plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
  apollo("octopus")
}

dependencies {
  implementation(libs.apollo.runtime)
  implementation(libs.arrow.core)
  implementation(libs.arrow.fx)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreDemoMode)
  implementation(projects.crossSells)
  implementation(projects.dataAddons)
  implementation(projects.dataContract)
  implementation(projects.dataCrossSellAfterFlow)
  implementation(projects.designSystemHedvig)
  implementation(projects.moleculePublic)
}
