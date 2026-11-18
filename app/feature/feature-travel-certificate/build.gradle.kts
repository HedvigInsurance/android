plugins {
  id("hedvig.multiplatform.library.ios")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus") {
    generateAsInternal = true
  }
  serialization()
  compose()
  navKeys()
  sharedWithIos()
  viewModels()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.apollo.normalizedCache)
      implementation(libs.apollo.runtime)
      implementation(libs.arrow.core)
      implementation(libs.coroutines.core)
      implementation(libs.jetbrains.compose.runtime)
      implementation(libs.kotlinx.datetime)
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreFileUpload)
      implementation(projects.dataAddons)
      implementation(projects.moleculePublic)
    }
    getByName("jvmAndAndroidMain").dependencies {
      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.compose.ui)
      implementation(libs.jetbrains.compose.ui.tooling.preview)
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(projects.composeUi)
      implementation(projects.coreLocale)
      implementation(projects.coreResources)
      implementation(projects.designSystemHedvig)
      implementation(projects.featureTravelCertificateNavigation)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCore)
    }
    nativeTest.dependencies {
      implementation(libs.assertK)
    }
    jvmTest.dependencies {
      implementation(libs.apollo.testingSupport)
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.jetbrains.compose.ui.test)
      implementation(libs.junit)
      implementation(libs.testParameterInjector)
      implementation(libs.turbine)
      implementation(org.jetbrains.compose.ComposePlugin.DesktopDependencies.currentOs)
      implementation(projects.apolloOctopusTest)
      implementation(projects.apolloTest)
      implementation(projects.coreCommonTest)
      implementation(projects.loggingTest)
      implementation(projects.moleculeTest)
    }
  }
}
