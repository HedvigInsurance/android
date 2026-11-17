plugins {
  id("hedvig.multiplatform.library.ios")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
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
      implementation(libs.arrow.fx)
      implementation(libs.jetbrains.compose.runtime)
      implementation(libs.kotlinx.datetime)
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreDemoMode)
      implementation(projects.coreUiData)
      implementation(projects.dataPayingMember)
      implementation(projects.featurePayinAccountNavigation)
      implementation(projects.moleculePublic)
    }
    getByName("jvmAndAndroidMain").dependencies {
      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.compose.ui.tooling.preview)
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(projects.composeUi)
      implementation(projects.coreResources)
      implementation(projects.designSystemHedvig)
      implementation(projects.featureForeverNavigation)
      implementation(projects.featurePaymentsNavigation)
      implementation(projects.memberRemindersUi)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCompose)
      implementation(projects.navigationCore)
      implementation(projects.pullrefresh)
    }
    jvmTest.dependencies {
      implementation(libs.apollo.testingSupport)
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.jetbrains.compose.ui.test)
      implementation(libs.junit)
      implementation(libs.turbine)
      implementation(org.jetbrains.compose.ComposePlugin.DesktopDependencies.currentOs)
      implementation(projects.apolloOctopusTest)
      implementation(projects.apolloTest)
      implementation(projects.coreCommonTest)
      implementation(projects.coreDatastoreTest)
      implementation(projects.dataSettingsDatastoreTest)
      implementation(projects.featureFlagsTest)
      implementation(projects.languageTest)
      implementation(projects.loggingTest)
      implementation(projects.memberRemindersTest)
      implementation(projects.moleculeTest)
    }
  }
}
