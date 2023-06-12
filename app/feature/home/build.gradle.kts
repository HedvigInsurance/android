plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("kotlin-parcelize")
  alias(libs.plugins.serialization)
}

android {
  namespace = "com.hedvig.android.feature.home"

  buildFeatures {
    viewBinding = true
  }
}

dependencies {
  implementation(projects.app.apollo.core)
  implementation(projects.app.apollo.di)
  implementation(projects.app.apollo.giraffe)
  implementation(projects.app.audioPlayer)
  implementation(projects.app.auth.authAndroid)
  implementation(projects.app.core.common)
  implementation(projects.app.core.commonAndroid)
  implementation(projects.app.core.designSystem)
  implementation(projects.app.core.resources)
  implementation(projects.app.core.ui)
  implementation(projects.app.data.travelCertificate)
  implementation(projects.app.hanalytics.hanalyticsCore)
  implementation(projects.app.hanalytics.hanalyticsFeatureFlags)
  implementation(projects.app.language.languageCore)
  implementation(projects.app.market.marketCore)
  implementation(projects.app.navigation.core)
  implementation(projects.app.navigation.navigationActivity)
  implementation(projects.app.navigation.navigationComposeTyped)

  testImplementation(projects.app.apollo.giraffeTest)
  testImplementation(projects.app.hanalytics.hanalyticsFeatureFlagsTest)

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
  implementation(libs.kiwi.navigationCompose)
  implementation(libs.koin.compose)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.materialComponents)
  implementation(libs.moneta)

  testImplementation(libs.assertK)
  testImplementation(libs.junit)
  testImplementation(libs.coroutines.test)
}
