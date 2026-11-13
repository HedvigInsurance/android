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
      implementation(libs.arrow.fx)
      implementation(libs.coroutines.core)
      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.compose.runtime)
      implementation(libs.jetbrains.compose.ui)
      implementation(libs.jetbrains.compose.ui.tooling.preview)
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(libs.kotlinx.datetime)
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreDemoMode)
      implementation(projects.coreFileUpload)
      implementation(projects.coreLocale)
      implementation(projects.coreResources)
      implementation(projects.dataAddons)
      implementation(projects.dataContract)
      implementation(projects.designSystemHedvig)
      implementation(projects.featureFlags)
      implementation(projects.languageCore)
      implementation(projects.featureTravelCertificateNavigation)
      implementation(projects.moleculePublic)
      implementation(projects.navigationActivity)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCore)
    }
    jvmTest.dependencies {
      implementation(libs.apollo.testingSupport)
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.junit)
      implementation(libs.testParameterInjector)
      implementation(projects.apolloOctopusTest)
      implementation(projects.apolloTest)
      implementation(projects.coreCommonTest)
      implementation(projects.featureFlagsTest)
      implementation(projects.loggingTest)
    }
  }
}
