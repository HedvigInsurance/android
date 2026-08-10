import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
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
      implementation(libs.arrow.core)
      implementation(libs.arrow.fx)
      implementation(libs.coroutines.core)
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreResources)
      implementation(projects.dataCoinsured)
      implementation(projects.featureFlags)
      implementation(projects.loggingPublic)
      implementation(projects.navigationCommon)
      implementation(projects.partnersDeflect)
      implementation(projects.uiEmergency)
      // -navigation modules for QuickLinkDestination.toNavKey() (Task 3)
      implementation(projects.featureChooseTierNavigation)
      implementation(projects.featureConnectPaymentTrustlyNavigation)
      implementation(projects.featureEditCoinsuredNavigation)
      implementation(projects.featureMovingflowNavigation)
      implementation(projects.featureTerminateInsuranceNavigation)
      implementation(projects.featureTravelCertificateNavigation)
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
      implementation(projects.loggingTest)
    }
  }
}
