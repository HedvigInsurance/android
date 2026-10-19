plugins {
  id("hedvig.multiplatform.library.ios")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  androidResources()
  apollo("octopus")
  compose()
  serialization()
  navKeys()
  viewModels()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.apollo.runtime)
      implementation(libs.apollo.normalizedCache)
      implementation(libs.arrow.core)
      implementation(libs.arrow.fx)
      implementation(libs.coroutines.core)
      implementation(libs.jetbrains.compose.ui.tooling.preview)
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(libs.jetbrains.navigationevent.compose)
      implementation(libs.kotlinx.serialization.core)
      implementation(libs.mikepenz.markdown)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.composeUi)
      implementation(projects.coreBuildConstants)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreResources)
      implementation(projects.dataCoinsured)
      implementation(projects.dataContract)
      implementation(projects.dataConversations)
      implementation(projects.dataTermination)
      implementation(projects.designSystemHedvig)
      implementation(projects.featureChooseTierNavigation)
      implementation(projects.featureConnectPaymentTrustlyNavigation)
      implementation(projects.featureEditCoinsuredNavigation)
      implementation(projects.featureFlags)
      implementation(projects.featureHelpCenterNavigation)
      implementation(projects.featureMovingflowNavigation)
      implementation(projects.featureTerminateInsuranceNavigation)
      implementation(projects.featureTravelCertificateNavigation)
      implementation(projects.moleculePublic)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCompose)
      implementation(projects.navigationCore)
      implementation(projects.memberQuickActions)
      implementation(projects.partnersDeflect)
      implementation(projects.uiEmergency)
    }
    jvmMain.dependencies {
    }
    jvmTest.dependencies {
      implementation(libs.apollo.testingSupport)
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.junit)
      implementation(libs.turbine)
      implementation(projects.apolloOctopusTest)
      implementation(projects.apolloTest)
      implementation(projects.coreCommonTest)
      implementation(projects.featureFlagsTest)
      implementation(projects.languageTest)
      implementation(projects.loggingTest)
      implementation(projects.memberRemindersTest)
      implementation(projects.moleculeTest)
      implementation(projects.testClock)
    }
  }
}
