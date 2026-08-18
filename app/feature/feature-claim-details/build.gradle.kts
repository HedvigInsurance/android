plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
  serialization()
  compose()
  navKeys()
  viewModels()
}

android {
  testOptions.unitTests.isReturnDefaultValues = true
}

dependencies {
  api(libs.coil.coil)

  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.apollo.normalizedCache)
  implementation(libs.apollo.runtime)
  implementation(libs.arrow.core)
  implementation(libs.arrow.fx)
  implementation(libs.jetbrains.lifecycle.runtime.compose)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.apolloCore)
  implementation(projects.apolloNetworkCacheManager)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.audioPlayerData)
  implementation(projects.audioPlayerUi)
  implementation(projects.claimStatus)
  implementation(projects.composePhotoCaptureState)
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
