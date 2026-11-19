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
      implementation(libs.apollo.runtime)
      implementation(libs.arrow.core)
      implementation(libs.coil.coil)
      implementation(libs.coroutines.core)
      implementation(libs.jetbrains.compose.ui.tooling.preview)
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(libs.kotlinx.datetime)
      implementation(libs.kotlinx.serialization.core)
      implementation(libs.kotlinx.serialization.json)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.composeUi)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreResources)
      implementation(projects.coreUiData)
      implementation(projects.dataContract)
      implementation(projects.dataProductVariantPublic)
      implementation(projects.designSystemHedvig)
      implementation(projects.featureInAppPurchaseNavigation)
      implementation(projects.moleculePublic)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCompose)
      implementation(projects.uiBankid)
      implementation(projects.uiTiersAndAddons)
    }
    jvmTest.dependencies {
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.junit)
      implementation(libs.turbine)
      implementation(projects.loggingTest)
      implementation(projects.moleculeTest)
    }
  }
}
