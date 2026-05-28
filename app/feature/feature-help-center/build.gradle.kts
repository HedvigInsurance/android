import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  androidResources()
  apollo("octopus")
  compose()
  serialization()
}

kotlin {
  sourceSets {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
      common {
        group("jvmAndAndroid") {
          withAndroidLibraryTarget()
          withJvm()
        }
      }
    }
    commonMain.dependencies {
      implementation(libs.apollo.runtime)
      implementation(libs.apollo.normalizedCache)
      implementation(libs.arrow.core)
      implementation(libs.arrow.fx)
      implementation(libs.coroutines.core)
      implementation(libs.jetbrains.compose.ui.tooling.preview)
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(libs.jetbrains.navigation.common)
      implementation(libs.jetbrains.navigation.compose)
      implementation(libs.jetbrains.navigationevent.compose)
      implementation(libs.koin.composeViewModel)
      implementation(libs.koin.core)
      implementation(libs.kotlinx.serialization.core)
      implementation(libs.mikepenz.markdown)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.composeUi)
      implementation(projects.coreBuildConstants)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreResources)
      implementation(projects.dataContract)
      implementation(projects.dataConversations)
      implementation(projects.dataTermination)
      implementation(projects.designSystemHedvig)
      implementation(projects.featureFlags)
      implementation(projects.moleculePublic)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCompose)
      implementation(projects.navigationComposeTyped)
      implementation(projects.navigationCore)
      implementation(projects.partnersDeflect)
      implementation(projects.uiEmergency)
    }
    androidMain.dependencies {
      implementation(libs.bundles.kmpPreviewBugWorkaround)
    }
    jvmMain.dependencies {
    }
    androidInstrumentedTest.dependencies {
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
