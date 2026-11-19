plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
  compose()
  serialization()
  navKeys()
  viewModels()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.apollo.normalizedCache)
      implementation(libs.arrow.core)
      implementation(libs.coil.compose)
      implementation(libs.coroutines.core)
      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.compose.material3.windowSizeClass)
      implementation(libs.jetbrains.compose.ui.tooling.preview)
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(libs.jetbrains.lifecycle.viewmodel.compose)
      implementation(libs.kotlinx.datetime)
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.composeUi)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreLocale)
      implementation(projects.coreResources)
      implementation(projects.coreUiData)
      implementation(projects.dataChangetier)
      implementation(projects.dataContract)
      implementation(projects.dataTermination)
      implementation(projects.designSystemHedvig)
      implementation(projects.featureFlags)
      implementation(projects.featureTerminateInsuranceNavigation)
      implementation(projects.languageCore)
      implementation(projects.moleculePublic)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCompose)
      implementation(projects.navigationCore)
      implementation(projects.uiTiersAndAddons)
    }
    jvmTest.dependencies {
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.junit)
      implementation(libs.turbine)
      implementation(projects.coreCommonTest)
      implementation(projects.dataProductVariantPublic)
      implementation(projects.loggingTest)
      implementation(projects.moleculeTest)
    }
  }
}
