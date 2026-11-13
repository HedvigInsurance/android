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
      api(libs.coil.coil)

      implementation(libs.apollo.normalizedCache)
      implementation(libs.arrow.core)
      implementation(libs.arrow.fx)
      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.compose.runtime)
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
      implementation(projects.crossSells)
      implementation(projects.dataAddons)
      implementation(projects.dataContract)
      implementation(projects.dataDisplayItems)
      implementation(projects.dataProductVariantPublic)
      implementation(projects.designSystemHedvig)
      implementation(projects.experimentsPublic)
      implementation(projects.featureFlags)
      implementation(projects.languageCore)
      implementation(projects.moleculePublic)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCompose)
      implementation(projects.navigationCore)
      implementation(projects.notificationBadgeDataPublic)
      implementation(projects.pullrefresh)
      implementation(projects.uiEmergency)
    }
    jvmTest.dependencies {
      implementation(libs.apollo.testingSupport)
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.junit)
      implementation(libs.testParameterInjector)
      implementation(libs.turbine)
      implementation(projects.apolloOctopusTest)
      implementation(projects.apolloTest)
      implementation(projects.coreCommonTest)
      implementation(projects.experimentsTest)
      implementation(projects.featureFlagsTest)
      implementation(projects.languageTest)
      implementation(projects.loggingTest)
      implementation(projects.moleculeTest)
      implementation(projects.trackingTest)
    }
  }
}
