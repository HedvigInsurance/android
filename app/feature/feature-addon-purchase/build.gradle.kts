plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
  serialization()
  compose()
  navKeys()
  viewModels()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.apollo.normalizedCache)
      implementation(libs.apollo.testingSupport)
      implementation(libs.arrow.core)
      implementation(libs.arrow.fx)
      implementation(libs.jetbrains.compose.ui.tooling.preview)
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.composeUi)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreDemoMode)
      implementation(projects.coreResources)
      implementation(projects.coreUiData)
      implementation(projects.dataAddons)
      implementation(projects.dataCrossSellAfterFlow)
      implementation(projects.dataProductVariantPublic)
      implementation(projects.designSystemHedvig)
      implementation(projects.featureFlags)
      implementation(projects.languageCore)
      implementation(projects.moleculePublic)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCompose)
      implementation(projects.navigationCore)
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
    }
  }
}
