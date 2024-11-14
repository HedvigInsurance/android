plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

hedvig {
  apollo("octopus")
  serialization()
  compose()
}

android {
  testOptions.unitTests.isReturnDefaultValues = true
}

dependencies {
  api(libs.androidx.navigation.common)
  api(libs.coil.coil)

  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.apollo.normalizedCache)
  implementation(libs.apollo.runtime)
  implementation(libs.arrow.core)
  implementation(libs.arrow.fx)
  implementation(libs.koin.compose)
  implementation(libs.koin.core)
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
  implementation(projects.coreCommonAndroidPublic)
  implementation(projects.coreDemoMode)
  implementation(projects.coreFileUpload)
  implementation(projects.coreResources)
  implementation(projects.coreRetrofit)
  implementation(projects.coreUiData)
  implementation(projects.designSystemHedvig)
  implementation(projects.featureFlagsPublic)
  implementation(projects.moleculeAndroid)
  implementation(projects.moleculePublic)
  implementation(projects.navigationCompose)
  implementation(projects.navigationCore)

}
