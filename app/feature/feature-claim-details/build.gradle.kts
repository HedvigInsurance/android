plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.apollo)
  alias(libs.plugins.serialization)
  alias(libs.plugins.squareSortDependencies)
}

android {
  testOptions.unitTests.isReturnDefaultValues = true
}

dependencies {
  apolloMetadata(projects.apolloOctopusPublic)

  implementation(projects.coreCommonAndroidPublic)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreRetrofit)
  implementation(libs.arrow.fx)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.navigation.common)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.apollo.normalizedCache)
  implementation(libs.apollo.runtime)
  implementation(libs.arrow.core)
  implementation(libs.kiwi.navigationCompose)
  implementation(libs.koin.compose)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.immutable.collections)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.coil.coil)
  implementation(libs.retrofitKotlinxSerializationConverter)
  implementation(libs.coil.compose)
  implementation(libs.retrofit)
  implementation(libs.retrofitArrow)
  implementation(projects.apolloCore)
  implementation(projects.placeholder)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.audioPlayer)
  implementation(projects.claimStatus)
  implementation(projects.coreDemoMode)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreIcons)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
  implementation(projects.coreUiData)
  implementation(projects.moleculeAndroid)
  implementation(projects.moleculePublic)
  implementation(projects.navigationCore)
  implementation(projects.composePhotoCaptureState)
}

apollo {
  service("octopus") {
    packageName.set("octopus")
  }
}
