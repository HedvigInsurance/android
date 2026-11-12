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
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.apolloCore)
      implementation(projects.uiPhoneNumber)
      implementation(projects.apolloNetworkCacheManager)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.authCorePublic)
      implementation(projects.composeUi)
      implementation(projects.coreBuildConstants)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreDatastorePublic)
      implementation(projects.coreDemoMode)
      implementation(projects.coreIcons)
      implementation(projects.coreLocale)
      implementation(projects.coreResources)
      implementation(projects.coreUiData)
      implementation(projects.dataSettingsDatastorePublic)
      implementation(projects.designSystemHedvig)
      implementation(projects.featureFlags)
      implementation(projects.featureProfileNavigation)
      implementation(projects.languageCore)
      implementation(projects.memberRemindersPublic)
      implementation(projects.memberRemindersUi)
      implementation(projects.moleculePublic)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCompose)
      implementation(projects.navigationCore)
      implementation(projects.notificationPermission)
      implementation(projects.pullrefresh)
      implementation(projects.theme)
      implementation(projects.uiAnalyticsConsent)
    }
    androidMain.dependencies {
      implementation(libs.androidx.other.appCompat)
      implementation(projects.composeWebview)
    }
    jvmTest.dependencies {
      implementation(libs.apollo.testingSupport)
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.junit)
      implementation(libs.testParameterInjector)
      implementation(libs.turbine)
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
      implementation(projects.testClock)
    }
  }
}
