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
      implementation(libs.apollo.api)
      implementation(libs.apollo.runtime)
      implementation(libs.arrow.core)
      implementation(libs.coroutines.core)
      implementation(libs.jetbrains.compose.animation)
      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.compose.foundation.layout)
      implementation(libs.jetbrains.compose.runtime)
      implementation(libs.jetbrains.compose.ui)
      implementation(libs.jetbrains.compose.ui.graphics)
      implementation(libs.jetbrains.compose.ui.tooling.preview)
      implementation(libs.jetbrains.compose.ui.unit)
      implementation(libs.jetbrains.lifecycle.common)
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(libs.jetbrains.lifecycle.viewmodel)
      implementation(libs.jetbrains.lifecycle.viewmodel.compose)
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.apolloCore)
      implementation(projects.apolloNetworkCacheManager)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.coreBuildConstants)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreResources)
      implementation(projects.designSystemHedvig)
      implementation(projects.featureConnectPaymentTrustlyNavigation)
      implementation(projects.moleculePublic)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCore)
    }
    androidMain.dependencies {
      implementation(libs.androidx.other.browser)
      implementation(projects.composeWebview)
    }
    jvmTest.dependencies {
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.junit)
      implementation(projects.authCorePublic)
      implementation(projects.coreDatastorePublic)
      implementation(projects.coreDemoMode)
      implementation(projects.languageCore)
      implementation(projects.loggingTest)
      implementation(projects.networkClients)
    }
  }
}
