plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
  apollo("octopus")
  viewModels()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.apollo.runtime)
      implementation(libs.apollo.normalizedCache)
      implementation(libs.arrow.core)
      implementation(libs.arrow.fx)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreDemoMode)
      implementation(projects.crossSells)
      implementation(projects.dataAddons)
      implementation(projects.dataContract)
      implementation(projects.dataCrossSellAfterFlow)
      implementation(projects.designSystemHedvig)
      implementation(projects.experimentsPublic)
      implementation(projects.moleculePublic)
    }
    jvmTest.dependencies {
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.junit)
      implementation(libs.turbine)
      implementation(projects.moleculeTest)
    }
  }
}
