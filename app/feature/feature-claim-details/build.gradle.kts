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
      implementation(libs.apollo.runtime)
      implementation(libs.arrow.core)
      implementation(libs.arrow.fx)
      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.compose.runtime)
      implementation(libs.jetbrains.compose.ui)
      implementation(libs.jetbrains.compose.ui.tooling.preview)
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(libs.kotlinx.datetime)
      implementation(libs.kotlinx.serialization.core)
      implementation(libs.uri.kmp)
      implementation(projects.apolloCore)
      implementation(projects.apolloNetworkCacheManager)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.audioPlayerData)
      implementation(projects.audioPlayerUi)
      implementation(projects.claimStatus)
      implementation(projects.composePhotoCaptureState)
      implementation(projects.composeResultLauncher)
      implementation(projects.composeUi)
      implementation(projects.coreBuildConstants)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreDemoMode)
      implementation(projects.coreFileUpload)
      implementation(projects.coreResources)
      implementation(projects.coreUiData)
      implementation(projects.dataCrossSellAfterClaimClosed)
      implementation(projects.dataDisplayItems)
      implementation(projects.designSystemHedvig)
      implementation(projects.featureFlags)
      implementation(projects.fileUploadUi)
      implementation(projects.moleculePublic)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCompose)
      implementation(projects.navigationCore)
    }
    androidMain.dependencies {
      implementation(libs.androidx.other.coreKtx)
    }
    jvmTest.dependencies {
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.junit)
      implementation(projects.authCorePublic)
      implementation(projects.coreDatastorePublic)
      implementation(projects.dataCrossSellAfterFlow)
      implementation(projects.languageCore)
      implementation(projects.loggingTest)
      implementation(projects.networkClients)
    }
  }
}
