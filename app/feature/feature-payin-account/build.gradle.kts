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
  android {
    withHostTest {
      isReturnDefaultValues = true
    }
  }
  sourceSets {
    commonMain.dependencies {
      implementation(libs.apollo.normalizedCache)
      implementation(libs.apollo.runtime)
      implementation(libs.arrow.core)
      implementation(libs.coroutines.core)
      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.compose.runtime)
      implementation(libs.jetbrains.compose.runtime.saveable)
      implementation(libs.jetbrains.compose.ui)
      implementation(libs.jetbrains.compose.ui.tooling.preview)
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(libs.jetbrains.lifecycle.viewmodel.compose)
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.apolloCore)
      implementation(projects.apolloNetworkCacheManager)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.coreBuildConstants)
      implementation(projects.composeUi)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreResources)
      implementation(projects.dataPayingMember)
      implementation(projects.designSystemHedvig)
      implementation(projects.featurePayinAccountNavigation)
      implementation(projects.moleculePublic)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCore)
    }
    jvmTest.dependencies {
      implementation(libs.apollo.testingSupport)
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.junit)
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.turbine)
      implementation(projects.apolloOctopusTest)
      implementation(projects.apolloTest)
      implementation(projects.loggingTest)
      implementation(projects.moleculeTest)
    }
    getByName("androidHostTest").dependencies {
      implementation(libs.androidx.junit)
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.junit)
      implementation(libs.robolectric)
      implementation(libs.turbine)
      implementation(projects.loggingTest)
      implementation(projects.moleculeTest)
    }
  }
}
