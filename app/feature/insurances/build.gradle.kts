plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.serialization)
}

android {
  namespace = "com.hedvig.android.feature.insurances"
  testOptions.unitTests.isReturnDefaultValues = true
}

dependencies {
  implementation(projects.app.apollo.core)
  implementation(projects.app.apollo.giraffe)
  implementation(projects.app.apollo.octopus)
  implementation(projects.app.core.common)
  implementation(projects.app.core.commonAndroid)
  implementation(projects.app.core.designSystem)
  implementation(projects.app.core.icons)
  implementation(projects.app.core.resources)
  implementation(projects.app.core.ui)
  implementation(projects.app.hanalytics.hanalyticsCore)
  implementation(projects.app.hanalytics.hanalyticsFeatureFlags)
  implementation(projects.app.language.languageCore)
  implementation(projects.app.navigation.core)
  implementation(projects.app.navigation.navigationComposeTyped)
  implementation(projects.app.notificationBadgeData.public)

  testImplementation(projects.app.notificationBadgeData.fake)

  implementation(libs.androidx.compose.material) // for pull to refresh
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.apollo.normalizedCache)
  implementation(libs.arrow.core)
  implementation(libs.arrow.fx)
  implementation(libs.coil.coil)
  implementation(libs.coil.compose)
  implementation(libs.koin.compose)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.immutable.collections)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.slimber)

  implementation(libs.assertK)
  implementation(libs.junit)

  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.turbine)
}
