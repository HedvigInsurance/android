plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
  serialization()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.androidx.datastore.core)
      implementation(libs.androidx.datastore.preferencesCore)
      implementation(libs.apollo.runtime)
      implementation(libs.arrow.core)
      implementation(libs.kotlinx.datetime)
      implementation(libs.kotlinx.serialization.core)
      implementation(libs.kotlinx.serialization.json)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreUiData)
      implementation(projects.dataContract)
      implementation(projects.dataCrossSellAfterFlow)
      implementation(projects.dataProductVariantPublic)
      implementation(projects.featureFlags)
      implementation(projects.uiTiersAndAddons)
    }
    jvmTest.dependencies {
      implementation(libs.apollo.testingSupport)
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.junit)
      implementation(libs.turbine)
      implementation(projects.apolloOctopusTest)
      implementation(projects.apolloTest)
      implementation(projects.coreCommonTest)
      implementation(projects.featureFlagsTest)
      implementation(projects.languageTest)
      implementation(projects.loggingTest)
      implementation(projects.moleculeTest)
      implementation(projects.testClock)
    }
  }
}
