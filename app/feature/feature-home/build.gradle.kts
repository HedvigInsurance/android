plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("kotlin-parcelize")
  alias(libs.plugins.serialization)
  alias(libs.plugins.squareSortDependencies)
}

android {
  namespace = "com.hedvig.android.feature.home"

  buildFeatures {
    viewBinding = true
  }
}

dependencies {
  implementation(projects.apolloCore)
  implementation(projects.apolloGiraffePublic)
  implementation(projects.audioPlayer)
  implementation(projects.authAndroid)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreCommonAndroidPublic)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
  implementation(projects.dataTravelCertificate)
  implementation(projects.hanalyticsCore)
  implementation(projects.hanalyticsFeatureFlagsPublic)
  implementation(projects.languageCore)
  implementation(projects.marketCore)
  implementation(projects.navigationCore)
  implementation(projects.navigationActivity)
  implementation(projects.navigationComposeTyped)

  testImplementation(projects.apolloGiraffeTest)
  testImplementation(projects.hanalyticsFeatureFlagsTest)

  implementation(libs.accompanist.pagerIndicators)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.material)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.uiViewBinding)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.lifecycle.runtime)
  implementation(libs.androidx.navigation.common)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.apollo.normalizedCache)
  implementation(libs.apollo.runtime)
  implementation(libs.arrow.core)
  implementation(libs.arrow.fx)
  implementation(libs.coil.compose)
  implementation(libs.fragmentViewBindingDelegate)
  implementation(libs.insetter)
  implementation(libs.koin.compose)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.materialComponents)
  implementation(libs.moneta)

  testImplementation(libs.assertK)
  testImplementation(libs.junit)
  testImplementation(libs.coroutines.test)
}
