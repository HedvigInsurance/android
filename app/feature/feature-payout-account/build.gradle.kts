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
      implementation(projects.apolloCore)
      implementation(projects.uiPhoneNumber)
      implementation(projects.apolloNetworkCacheManager)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.coreBuildConstants)
      implementation(libs.jetbrains.compose.runtime)
      implementation(libs.jetbrains.compose.ui.tooling.preview)
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(projects.composeUi)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreResources)
      implementation(projects.dataPayingMember)
      implementation(projects.designSystemHedvig)
      implementation(projects.moleculePublic)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCompose)
      implementation(projects.navigationCore)
    }
    jvmTest.dependencies {
      implementation(libs.apollo.testingSupport)
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.junit)
      implementation(libs.turbine)
      implementation(projects.apolloOctopusTest)
      implementation(projects.apolloTest)
      implementation(projects.loggingTest)
      implementation(projects.moleculeTest)
    }
  }
}
